package grails.plugin.json.view.api.internal

import grails.core.support.proxy.ProxyHandler
import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.builder.StreamingJsonBuilder.StreamingJsonDelegate
import grails.plugin.json.view.api.GrailsJsonViewHelper
import grails.plugin.json.view.api.JsonView
import grails.util.GrailsClassUtils
import grails.util.GrailsNameUtils
import grails.views.ViewException
import grails.views.api.GrailsView
import grails.views.api.internal.DefaultGrailsViewHelper
import grails.views.mvc.renderer.DefaultViewRenderer
import groovy.text.Template
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.beans.support.CachedIntrospectionResults
import org.grails.buffer.FastStringWriter
import org.grails.core.util.ClassPropertyFetcher
import org.grails.core.util.IncludeExcludeSupport
import org.grails.datastore.mapping.collection.PersistentCollection
import org.grails.datastore.mapping.model.MappingFactory
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.datastore.mapping.model.types.ToOne
import org.springframework.util.ReflectionUtils

/**
 * Extended version of {@link DefaultGrailsViewHelper} with methods specific to JSON view rendering
 *
 * @author Graeme Rocher
 */
@CompileStatic
@InheritConstructors
class JsonGrailsViewHelper extends DefaultGrailsViewHelper implements GrailsJsonViewHelper {

    private static final String DEEP = "deep"
    public static final String BEFORE_CLOSURE = "beforeClosure"

    /**
     * Default includes/excludes for GORM properties
     */
    IncludeExcludeSupport<String> includeExcludeSupport = new IncludeExcludeSupport<String>(null, ["class", 'metaClass', 'properties', "version", "attached", "errors", "dirty"]) {
        @Override
        boolean shouldInclude(List<String> incs, List excs, String object) {
            def i = object.lastIndexOf('.')
            String unqualified = i > -1 ? object.substring(i + 1) : null
            return super.shouldInclude(incs, excs, object) && (unqualified == null || (includes(defaultIncludes, unqualified) && !excludes(defaultExcludes, unqualified)))
        }
    }

    @Override
    JsonOutput.JsonUnescaped render(Object object, @DelegatesTo(StreamingJsonDelegate) Closure customizer) {
        render object, Collections.emptyMap(), customizer
    }

    @Override
    JsonOutput.JsonUnescaped render(Object object, Map arguments = Collections.emptyMap(), @DelegatesTo(StreamingJsonDelegate) Closure customizer = null ) {
        JsonView jsonView = (JsonView)view
        object = jsonView.proxyHandler?.unwrapIfProxy(object) ?: object
        if(object == null) {
            return JsonOutput.unescaped("null")
        }

        def entity = jsonView.mappingContext?.getPersistentEntity(object.getClass().name)
        def writer = new FastStringWriter()
        List<String> incs = (List<String>)arguments.get(IncludeExcludeSupport.INCLUDES_PROPERTY) ?: null
        List<String> excs = (List<String>)arguments.get(IncludeExcludeSupport.EXCLUDES_PROPERTY) ?: new ArrayList<String>()

        boolean isDeep = GrailsClassUtils.getBooleanFromMap(DEEP, arguments)
        Closure beforeClosure = (Closure)arguments.get(BEFORE_CLOSURE)
        StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
        Set processedObjects = []

        if(entity != null) {

            builder.call {
                StreamingJsonDelegate jsonDelegate = (StreamingJsonDelegate)getDelegate()
                if(beforeClosure != null) {
                    beforeClosure.setDelegate(jsonDelegate)
                    beforeClosure.call()
                }
                process(jsonDelegate, entity, object, processedObjects, incs, excs, "", isDeep)
                if(customizer != null) {
                    customizer.setDelegate(jsonDelegate)
                    customizer.call()
                }
            }
        }
        else {
            builder.call {
                StreamingJsonDelegate jsonDelegate = (StreamingJsonDelegate)getDelegate()
                if(beforeClosure != null) {
                    beforeClosure.setDelegate(jsonDelegate)
                    beforeClosure.call()
                }
                processSimple(jsonDelegate, object, processedObjects, incs, excs, "")
                if(customizer != null) {
                    customizer.setDelegate(jsonDelegate)
                    customizer.call()
                }
            }
        }
        return JsonOutput.unescaped(writer.toString())
    }

    protected void processSimple(StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate, Object object, Set processedObjects, List<String> incs, List<String> excs, String path) {

        if(!processedObjects.contains(object)) {
            processedObjects.add(object)

            def cpf = ClassPropertyFetcher.forClass(object.getClass())
            def descriptors = cpf.getPropertyDescriptors()
            for (desc in descriptors) {
                def readMethod = desc.readMethod
                if (readMethod && desc.writeMethod) {
                    def name = desc.name
                    String qualified = "${path}${name}"
                    if (includeExcludeSupport.shouldInclude(incs, excs, qualified)) {
                        def value = cpf.getPropertyValue(object, desc.name)
                        if(value != null) {
                            boolean isArray = value.getClass().isArray()
                            if(MappingFactory.isSimpleType(desc.propertyType.name) || (value instanceof Map)) {
                                jsonDelegate.call name, value
                            }

                            else if(isArray || (value instanceof Iterable)) {
                                Iterable iterable = isArray ? value as List : (Iterable)value
                                jsonDelegate.call(name, iterable) { child ->
                                    StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                    processSimple(embeddedDelegate, child, processedObjects, incs, excs,"${path}${name}.")
                                }
                            }
                            else {
                                if(!processedObjects.contains(value)) {
                                    jsonDelegate.call( name ) {
                                        jsonDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                        processSimple(jsonDelegate, value, processedObjects, incs, excs,"${path}${name}.")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void process(StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate, PersistentEntity entity, Object object, Set processedObjects, List<String> incs, List<String> excs, String path, boolean isDeep) {

        if(processedObjects.contains(object))return
        processedObjects.add(object)


        def idName = entity.identity?.name
        String idQualified = "${path}${idName}"
        if(includeExcludeSupport.shouldInclude(incs, excs, idQualified)) {

            def idValue = ((GroovyObject) object).getProperty(idName)
            if(idValue != null) {
                jsonDelegate.call(idName, idValue)
            }
        }

        for (prop in entity.persistentProperties) {
            def propertyName = prop.name
            String qualified = "${path}${propertyName}"
            if (!includeExcludeSupport.shouldInclude(incs, excs, qualified)) continue

            def value = ((GroovyObject) object).getProperty(propertyName)
            if(value == null) continue

            if (!(prop instanceof Association)) {
                jsonDelegate.call(propertyName, value)
            } else {
                Association ass = (Association) prop
                def associatedEntity = ass.associatedEntity

                if (ass instanceof Embedded) {
                    def propertyType = ass.type
                    def childTemplate = view.templateEngine?.resolveTemplate(DefaultViewRenderer.templateNameForClass(propertyType), view.locale)
                    if(childTemplate != null) {
                        def childView = prepareWritable(childTemplate, [(GrailsNameUtils.getPropertyName(propertyType)): value])
                        def writer = new FastStringWriter()
                        childView.writeTo(writer)
                        jsonDelegate.call(propertyName, JsonOutput.unescaped(writer.toString()))
                    }
                    else {
                        jsonDelegate.call(propertyName) {
                            StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                            if(associatedEntity != null) {
                                process(embeddedDelegate, associatedEntity,value, processedObjects, incs, excs , "${qualified}.", isDeep)
                            }
                            else {
                                processSimple(embeddedDelegate, value, processedObjects, incs, excs, "${qualified}.")
                            }
                        }
                    }
                }
                else if(ass instanceof ToOne) {
                    if(associatedEntity != null) {
                        def associationIdName = associatedEntity.identity.name

                        def propertyType = ass.type
                        def childTemplate = view.templateEngine?.resolveTemplate(DefaultViewRenderer.templateNameForClass(propertyType), view.locale)
                        if(childTemplate != null) {
                            def childView = prepareWritable(childTemplate, [(GrailsNameUtils.getPropertyName(propertyType)): value])
                            def writer = new FastStringWriter()
                            childView.writeTo(writer)
                            jsonDelegate.call(propertyName, JsonOutput.unescaped(writer.toString()))
                        }
                        else {
                            JsonView jsonView = (JsonView)view
                            ProxyHandler proxyHandler = jsonView.proxyHandler
                            def associatedId = ((GroovyObject)value).getProperty(associationIdName)

                            if(isDeep && (proxyHandler == null || proxyHandler.isInitialized(value))) {
                                jsonDelegate.call(propertyName) {
                                    StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                    process(embeddedDelegate, associatedEntity,value, processedObjects, incs, excs , "${qualified}.", isDeep)
                                }
                            }
                            else if(associatedId != null) {
                                jsonDelegate.call(propertyName) {
                                    call(associationIdName, associatedId)
                                }
                            }

                        }
                    }
                }
                else if(Iterable.isAssignableFrom(ass.type)) {
                    if(value instanceof PersistentCollection) {
                        PersistentCollection pc = (PersistentCollection)value
                        if(!pc.isInitialized()) continue
                    }
                    def propertyType = ass.type
                    def childTemplate = view.templateEngine?.resolveTemplate(DefaultViewRenderer.templateNameForClass(propertyType), view.locale)
                    if(childTemplate != null) {
                        def writer = new FastStringWriter()
                        def iterator = ((Iterable) value).iterator()
                        writer.write(JsonOutput.OPEN_BRACKET)
                        def childPropertyName = GrailsNameUtils.getPropertyName(propertyType)

                        while(iterator.hasNext()) {
                            def o = iterator.next()
                            def childView = prepareWritable(childTemplate, [(childPropertyName): o])
                            childView.writeTo(writer)
                            if(iterator.hasNext()) {
                                writer.write(JsonOutput.COMMA)
                            }
                        }
                        writer.write(JsonOutput.CLOSE_BRACKET)
                        jsonDelegate.call(propertyName, JsonOutput.unescaped(writer.toString()))
                    }
                    else {
                        def associationIdName = associatedEntity.identity.name
                        jsonDelegate.call(propertyName, (Iterable)value) { child ->
                            if(isDeep) {
                                StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                process(embeddedDelegate, associatedEntity,child, processedObjects, incs, excs , "${qualified}.", isDeep)
                            }
                            else {
                                def associatedId = ((GroovyObject)child).getProperty(associationIdName)
                                if(associatedId != null) {
                                    call(associationIdName, associatedId)
                                }
                                else {
                                    StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                    process(embeddedDelegate, associatedEntity,child, processedObjects, incs, excs , "${qualified}.", isDeep)
                                }
                            }
                        }
                    }

                }

            }
        }
    }

    @Override
    JsonOutput.JsonUnescaped render(Map arguments) {
        def template = arguments.template

        def templateEngine = view.templateEngine
        if(template) {
            Map model = (Map)arguments.model ?: [:]
            def collection = arguments.collection
            def var = arguments.var ?: 'it'
            def templateUri = templateEngine
                    .viewUriResolver
                    .resolveTemplateUri(view.getControllerName(), template.toString())
            def childTemplate = templateEngine.resolveTemplate(templateUri, view.locale)
            if(childTemplate != null) {
                FastStringWriter stringWriter = new FastStringWriter()
                if(collection instanceof Iterable) {
                    Iterable iterable = (Iterable)collection
                    int size = iterable.size()
                    int i = 0
                    stringWriter.append '['
                    for(o in collection) {
                        model.put(var, o)
                        def writable = prepareWritable(childTemplate, model)
                        writable.writeTo( stringWriter )
                        if(++i != size) {
                            stringWriter.append ','
                        }
                    }
                    stringWriter.append ']'
                }
                else {
                    GrailsView writable = prepareWritable(childTemplate, model)
                    writable.writeTo( stringWriter )
                }

                return JsonOutput.unescaped( stringWriter.toString() )
            }
            else {
                throw new ViewException("Template not found for name $template")
            }
        }

    }

    protected GrailsView prepareWritable(Template childTemplate, Map model) {
        GrailsView writable = (GrailsView) (model ? childTemplate.make((Map) model) : childTemplate.make())
        writable.locale = view.locale
        writable.response = view.response
        writable.request = view.request
        writable.controllerName = view.controllerName
        writable.actionName = view.actionName
        return writable
    }
}
