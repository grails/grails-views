package grails.plugin.json.view.api.internal

import grails.core.support.proxy.ProxyHandler
import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.builder.StreamingJsonBuilder.StreamingJsonDelegate
import grails.plugin.json.view.api.GrailsJsonViewHelper
import grails.plugin.json.view.api.JsonView
import grails.util.GrailsClassUtils
import grails.util.GrailsNameUtils
import grails.views.ResolvableGroovyTemplateEngine
import grails.views.ViewException
import grails.views.api.GrailsView
import grails.views.api.internal.DefaultGrailsViewHelper
import grails.views.resolve.TemplateResolverUtils
import groovy.text.Template
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.buffer.FastStringWriter
import org.grails.core.util.ClassPropertyFetcher
import org.grails.core.util.IncludeExcludeSupport
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.collection.PersistentCollection
import org.grails.datastore.mapping.model.MappingFactory
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Basic
import org.grails.datastore.mapping.model.types.Custom
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.datastore.mapping.model.types.EmbeddedCollection
import org.grails.datastore.mapping.model.types.ToMany
import org.grails.datastore.mapping.model.types.ToOne
import org.springframework.util.ReflectionUtils

import java.beans.PropertyDescriptor
import java.lang.reflect.ParameterizedType

/**
 * Extended version of {@link DefaultGrailsViewHelper} with methods specific to JSON view rendering
 *
 * @author Graeme Rocher
 */
@CompileStatic
@InheritConstructors
class DefaultGrailsJsonViewHelper extends DefaultGrailsViewHelper implements GrailsJsonViewHelper {


    private static final Set<String> TO_STRING_TYPES = [
        "org.bson.types.ObjectId"
    ] as Set
    public static final String BEFORE_CLOSURE = "beforeClosure"
    public static final String PROCESSED_OBJECT_VARIABLE = "org.json.views.RENDER_PROCESSED_OBJECTS"
    public static final String NULL_OUTPUT = "NULL_OUTPUT"

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

        def binding = jsonView.getBinding()
        object = jsonView.proxyHandler?.unwrapIfProxy(object) ?: object
        if(object == null) {
            return JsonOutput.unescaped("null")
        }

        def entity = findEntity(object)
        def writer = new FastStringWriter()

        boolean isDeep = GrailsClassUtils.getBooleanFromMap(DEEP, arguments)
        Closure beforeClosure = (Closure)arguments.get(BEFORE_CLOSURE)
        StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
        Map<Object, String> processedObjects
        boolean rootRender = false

        def bindingVariables = binding.variables
        if(bindingVariables.containsKey(PROCESSED_OBJECT_VARIABLE)) {
            processedObjects = (Map<Object,String>)binding.getVariable(PROCESSED_OBJECT_VARIABLE)
            if(processedObjects.containsKey(object)) {

                def existingOutput = processedObjects.get(object)
                if(!NULL_OUTPUT.equals(existingOutput)) {
                    return JsonOutput.unescaped(existingOutput)
                }
            }
        }
        else {
            processedObjects = new LinkedHashMap<Object, String>()
            binding.setVariable(PROCESSED_OBJECT_VARIABLE, processedObjects)
            rootRender = true
        }

        try {
            if(entity != null) {

                builder.call {
                    StreamingJsonDelegate jsonDelegate = (StreamingJsonDelegate)getDelegate()
                    if(beforeClosure != null) {
                        beforeClosure.setDelegate(jsonDelegate)
                        beforeClosure.call()
                    }
                    List<String> incs = (List<String>)arguments.get(IncludeExcludeSupport.INCLUDES_PROPERTY) ?: null
                    List<String> excs = (List<String>)arguments.get(IncludeExcludeSupport.EXCLUDES_PROPERTY) ?: new ArrayList<String>()

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
                    List<String> incs = (List<String>)arguments.get(IncludeExcludeSupport.INCLUDES_PROPERTY) ?: null
                    List<String> excs = (List<String>)arguments.get(IncludeExcludeSupport.EXCLUDES_PROPERTY) ?: new ArrayList<String>()

                    processSimple(jsonDelegate, object, processedObjects, incs, excs, "")
                    if(customizer != null) {
                        customizer.setDelegate(jsonDelegate)
                        customizer.call()
                    }
                }
            }


            def stringResult = writer.toString()
            def unescaped = JsonOutput.unescaped(stringResult)
            processedObjects.put(object, stringResult)
            return unescaped
        } finally {

            if(rootRender) {
                bindingVariables.remove(PROCESSED_OBJECT_VARIABLE)
            }
        }
    }

    protected PersistentEntity findEntity(Object object) {
        def clazz = object.getClass()
        try {
            return GormEnhancer.findEntity(clazz)
        } catch (Throwable e) {
            return ((JsonView)view)?.mappingContext?.getPersistentEntity(clazz.name)
        }
    }

    protected void processSimple(StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate, Object object, Map<Object, String> processedObjects, List<String> incs, List<String> excs, String path) {

        if(!processedObjects.containsKey(object)) {
            processedObjects.put(object, NULL_OUTPUT)


            def declaringClass = object.getClass()
            def cpf = ClassPropertyFetcher.forClass(declaringClass)
            def descriptors = cpf.getPropertyDescriptors()
            for (desc in descriptors) {
                def readMethod = desc.readMethod
                if (readMethod != null && desc.writeMethod != null) {
                    def propertyName = desc.name
                    String qualified = "${path}${propertyName}"
                    if (includeExcludeSupport.shouldInclude(incs, excs, qualified)) {
                        def value = cpf.getPropertyValue(object, desc.name)
                        if(value != null) {
                            def propertyType = desc.propertyType
                            boolean isArray = propertyType.isArray()
                            if(isStringType(propertyType)) {
                                jsonDelegate.call propertyName, value.toString()
                            }
                            else if(isSimpleType(propertyType, value)) {
                                jsonDelegate.call propertyName, value
                            }
                            else if(isArray || Iterable.isAssignableFrom(propertyType)) {
                                Class componentType

                                if(isArray) {
                                    componentType = propertyType.componentType
                                }
                                else {
                                    componentType = getGenericType(declaringClass, desc)
                                }

                                if(!Object.is(componentType) && MappingFactory.isSimpleType(componentType.name) || componentType.isEnum()) {
                                    jsonDelegate.call(propertyName, value)
                                }
                                else {
                                    Iterable iterable = isArray ? value as List : (Iterable)value
                                    jsonDelegate.call(propertyName, iterable) { child ->
                                        StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                        processSimple(embeddedDelegate, child, processedObjects, incs, excs,"${path}${propertyName}.")
                                    }
                                }
                            }
                            else {
                                if(!processedObjects.containsKey(value)) {
                                    ResolvableGroovyTemplateEngine templateEngine = view.templateEngine
                                    def childTemplate = templateEngine?.resolveTemplate(propertyType, view.locale)
                                    if(childTemplate != null) {
                                        JsonOutput.JsonUnescaped jsonUnescaped = renderChildTemplate(childTemplate, propertyType, value)
                                        jsonDelegate.call(propertyName, jsonUnescaped)
                                    }
                                    else {
                                        jsonDelegate.call( propertyName ) {
                                            jsonDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                            processSimple(jsonDelegate, value, processedObjects, incs, excs,"${path}${propertyName}.")
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected Class getGenericType(Class declaringClass, PropertyDescriptor descriptor) {
        def field = ReflectionUtils.findField(declaringClass, descriptor.getName())
        if(field != null) {

            def type = field.genericType
            if(type instanceof ParameterizedType) {
                def args = ((ParameterizedType) type).getActualTypeArguments()
                if(args.length > 0) {
                    def t = args[0]
                    if(t instanceof Class) {
                        return (Class)t
                    }
                }
            }
        }
        return Object
    }

    public static boolean isStringType(Class propertyType) {
        return TO_STRING_TYPES.contains(propertyType.name)
    }
    public static boolean isSimpleType(Class propertyType, value) {
        MappingFactory.isSimpleType(propertyType.name) || (value instanceof Enum) || (value instanceof Map)
    }

    protected void process(StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate, PersistentEntity entity, Object object, Map<Object, String> processedObjects, List<String> incs, List<String> excs, String path, boolean isDeep) {

        if(processedObjects.containsKey(object)) {
            return
        }
        processedObjects.put(object, NULL_OUTPUT)

        def idName = entity.identity?.name
        String idQualified = "${path}${idName}"
        if(includeExcludeSupport.shouldInclude(incs, excs, idQualified)) {

            def idValue = ((GroovyObject) object).getProperty(idName)
            if(idValue != null) {
                jsonDelegate.call(idName, idValue)
            }
        }
        ResolvableGroovyTemplateEngine templateEngine = view.templateEngine

        for (prop in entity.persistentProperties) {
            def propertyName = prop.name
            String qualified = "${path}${propertyName}"
            if (!includeExcludeSupport.shouldInclude(incs, excs, qualified)) continue

            def value = ((GroovyObject) object).getProperty(propertyName)
            if(value == null) continue

            def locale = view.locale
            if (!(prop instanceof Association)) {
                processSimpleProperty(jsonDelegate, (PersistentProperty) prop, propertyName, value, templateEngine, locale)
            } else {
                Association ass = (Association) prop
                def associatedEntity = ass.associatedEntity

                if (ass instanceof Embedded) {
                    def propertyType = ass.type
                    def childTemplate = templateEngine?.resolveTemplate(propertyType, locale)
                    if(childTemplate != null) {
                        JsonOutput.JsonUnescaped jsonUnescaped = renderChildTemplate(childTemplate, propertyType, value)
                        jsonDelegate.call(propertyName, jsonUnescaped)
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
                        JsonView jsonView = (JsonView)view
                        ProxyHandler proxyHandler = jsonView.proxyHandler
                        def associatedId = ((GroovyObject)value).getProperty(associationIdName)

                        def childTemplate = templateEngine?.resolveTemplate(TemplateResolverUtils.shortTemplateNameForClass(propertyType), locale)
                        if(childTemplate != null) {
                            def model = [(GrailsNameUtils.getPropertyName(propertyType)): value]
                            def childView = prepareWritable(childTemplate, model)
                            def writer = new FastStringWriter()
                            childView.writeTo(writer)
                            jsonDelegate.call(propertyName, JsonOutput.unescaped(writer.toString()))
                        }
                        else if(isDeep || proxyHandler?.isInitialized(value)) {
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
                else if((ass instanceof ToMany) && Iterable.isAssignableFrom(ass.type)) {

                    if(ass instanceof Basic) {
                        // basic collection types like lists of strings etc. just render directly
                        jsonDelegate.call(propertyName, value)
                    }
                    else {
                        if(!isDeep) {
                            def proxyHandler = ((JsonView) view).getProxyHandler()
                            if(proxyHandler?.isProxy(value) && !proxyHandler.isInitialized(value)) {
                                continue
                            }
                            if(value instanceof PersistentCollection) {
                                PersistentCollection pc = (PersistentCollection)value
                                if(!pc.isInitialized()) continue
                            }
                        }
                        def propertyType = ass.associatedEntity.javaClass
                        def childTemplate = templateEngine?.resolveTemplate(propertyType, locale)
                        if(childTemplate != null) {
                            def writer = new FastStringWriter()
                            def iterator = ((Iterable) value).iterator()
                            writer.write(JsonOutput.OPEN_BRACKET)
                            def childPropertyName = GrailsNameUtils.getPropertyName(propertyType)

                            while(iterator.hasNext()) {
                                def o = iterator.next()

                                def model = [(childPropertyName): o]
                                def childView = prepareWritable(childTemplate, model)
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
                else if(ass instanceof EmbeddedCollection) {
                    if(Iterable.isAssignableFrom(ass.type) && associatedEntity != null) {
                        jsonDelegate.call(propertyName, (Iterable)value) { child ->
                            StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                            process(embeddedDelegate, associatedEntity,child, processedObjects, incs, excs , "${qualified}.", isDeep)
                        }
                    }
                }

            }
        }
    }

    protected void processSimpleProperty(StreamingJsonDelegate jsonDelegate, PersistentProperty prop, String propertyName, Object value, ResolvableGroovyTemplateEngine templateEngine, Locale locale) {
        if (prop instanceof Custom) {
            def propertyType = value.getClass()
            def childTemplate = templateEngine.resolveTemplate(propertyType, locale)
            if (childTemplate != null) {
                JsonOutput.JsonUnescaped jsonUnescaped = renderChildTemplate(childTemplate, propertyType, value)
                jsonDelegate.call(propertyName, jsonUnescaped)
            } else {
                jsonDelegate.call(propertyName, value)
            }
        } else {
            if (isStringType(prop.type)) {
                jsonDelegate.call propertyName, value.toString()
            } else {
                jsonDelegate.call(propertyName, value)
            }
        }
    }

    JsonOutput.JsonUnescaped renderChildTemplate(Template template, Class modelType, modelValue) {
        def childView = prepareWritable(template, [(GrailsNameUtils.getPropertyName(modelType)): modelValue])
        def writer = new FastStringWriter()
        childView.writeTo(writer)
        def jsonUnescaped = JsonOutput.unescaped(writer.toString())
        jsonUnescaped
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

    protected void populateModelWithViewState(Map model) {
        def parentViewBinding = view.binding
        if (parentViewBinding.variables.containsKey(PROCESSED_OBJECT_VARIABLE)) {
            model.put(PROCESSED_OBJECT_VARIABLE, parentViewBinding.getVariable(PROCESSED_OBJECT_VARIABLE))
        }
    }

    protected GrailsView prepareWritable(Template childTemplate, Map model) {
        populateModelWithViewState(model)
        GrailsView writable = (GrailsView) (model ? childTemplate.make((Map) model) : childTemplate.make())
        writable.locale = view.locale
        writable.response = view.response
        writable.request = view.request
        writable.controllerName = view.controllerName
        writable.actionName = view.actionName
        return writable
    }
}
