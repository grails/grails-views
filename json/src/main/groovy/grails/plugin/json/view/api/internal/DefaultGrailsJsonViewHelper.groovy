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
import grails.views.ViewConfiguration
import grails.views.ViewException
import grails.views.api.GrailsView
import grails.views.api.internal.DefaultGrailsViewHelper
import grails.views.resolve.TemplateResolverUtils
import grails.views.utils.ViewUtils
import groovy.text.Template
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.buffer.FastStringWriter
import org.grails.core.util.ClassPropertyFetcher
import org.grails.core.util.IncludeExcludeSupport
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.collection.PersistentCollection
import org.grails.datastore.mapping.model.MappingContext
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
    public static final JsonOutput.JsonWritable NULL_OUTPUT = new JsonOutput.JsonWritable() {
        @Override
        Writer writeTo(Writer out) throws IOException {
            out.write(JsonOutput.NULL_VALUE);
            return out;
        }
    }

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
    JsonOutput.JsonWritable render(Object object, @DelegatesTo(StreamingJsonDelegate) Closure customizer) {
        render object, Collections.emptyMap(), customizer
    }

    @Override
    void inline(Object object, Map arguments = Collections.emptyMap(), @DelegatesTo(StreamingJsonDelegate) Closure customizer = null) {
        JsonView jsonView = (JsonView)view
        def jsonDelegate = new StreamingJsonDelegate(jsonView.out, true)
        Map<Object, JsonOutput.JsonWritable> processedObjects = initializeProcessedObjects(jsonView.binding)
        boolean isDeep = ViewUtils.getBooleanFromMap(DEEP, arguments)
        boolean includeAssociations = ViewUtils.getBooleanFromMap(ASSOCIATIONS, arguments, true)
        List<String> expandProperties = getExpandProperties(jsonView, arguments)

        List<String> incs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.INCLUDES_PROPERTY, arguments, null)
        List<String> excs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.INCLUDES_PROPERTY, arguments)


        def mappingContext = jsonView.mappingContext
        object = mappingContext.proxyHandler.unwrap(object)

        PersistentEntity entity = mappingContext.getPersistentEntity(object.getClass().name)

        if(entity != null) {
            process(jsonDelegate, entity, object, processedObjects, incs, excs, "", isDeep, expandProperties, includeAssociations, customizer)
        }
        else {
            processSimple(jsonDelegate, object, processedObjects, incs, excs, "", customizer)
        }


    }

    private List<String> getExpandProperties(JsonView jsonView, Map arguments) {
        List<String> expandProperties
        def templateEngine = jsonView.templateEngine
        def viewConfiguration = templateEngine?.viewConfiguration
        if (viewConfiguration == null || viewConfiguration.isAllowResourceExpansion()) {
            expandProperties = (List<String>) (jsonView.params.list(EXPAND) ?: ViewUtils.getStringListFromMap(EXPAND, arguments))
        } else {
            expandProperties = Collections.emptyList()
        }
        expandProperties
    }

    @Override
    void inline(Object object, @DelegatesTo(StreamingJsonDelegate) Closure customizer) {
        inline(object, Collections.emptyMap(), customizer)
    }

    private JsonOutput.JsonWritable preProcessedOutput(Object object, Map<Object, JsonOutput.JsonWritable> processedObjects) {
        JsonView jsonView = (JsonView)view
        boolean rootRender = processedObjects.isEmpty()
        object = jsonView.proxyHandler?.unwrapIfProxy(object) ?: object
        if(object == null) {
            return NULL_OUTPUT
        }

        if(!rootRender && processedObjects.containsKey(object)) {
            def existingOutput = processedObjects.get(object)
            if(!NULL_OUTPUT.equals(existingOutput)) {
                return existingOutput
            }
        }
        return null
    }

    private JsonOutput.JsonWritable renderTemplate(Object object, Map arguments, Closure customizer, Map<Object, JsonOutput.JsonWritable> processedObjects) {
        JsonOutput.JsonWritable preProcessed = preProcessedOutput(object, processedObjects)
        if (preProcessed != null) {
            return preProcessed
        }

        ResolvableGroovyTemplateEngine templateEngine = view.templateEngine
        Locale locale = view.locale
        Template childTemplate = templateEngine?.resolveTemplate(object.class, locale)
        if(childTemplate != null) {
            return renderChildTemplate(childTemplate, object.class, object)
        } else {
            return renderDefault(object, arguments, customizer, processedObjects)
        }
    }

    private JsonOutput.JsonWritable renderDefault(Object object, Map arguments, Closure customizer, Map<Object, JsonOutput.JsonWritable> processedObjects) {
        JsonOutput.JsonWritable preProcessed = preProcessedOutput(object, processedObjects)
        if (preProcessed != null) {
            return preProcessed
        }

        JsonView jsonView = (JsonView)view
        boolean rootRender = processedObjects.isEmpty()
        def binding = jsonView.getBinding()
        def entity = findEntity(object)

        final boolean isDeep = ViewUtils.getBooleanFromMap(DEEP, arguments)
        List<String> expandProperties = getExpandProperties(jsonView, arguments)
        final Closure beforeClosure = (Closure)arguments.get(BEFORE_CLOSURE)

        Closure doProcessEntity = { StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate, List<String> incs, List<String> excs ->
            process(jsonDelegate, entity, object, processedObjects, incs, excs, "", isDeep, expandProperties, true, customizer)
        }

        Closure doProcessSimple = { StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate, List<String> incs, List<String> excs ->
            processSimple(jsonDelegate, object, processedObjects, incs, excs, "", customizer)
        }

        def jsonWritable = new JsonOutput.JsonWritable() {
            @Override
            @CompileStatic
            Writer writeTo(Writer out) throws IOException {
                try {
                    if (entity != null) {

                        if(inline) {
                            StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate = new StreamingJsonBuilder.StreamingJsonDelegate(out, first)
                            if (beforeClosure != null) {
                                beforeClosure.setDelegate(jsonDelegate)
                                beforeClosure.call()
                            }
                            List<String> incs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.INCLUDES_PROPERTY, arguments, null)
                            List<String> excs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.EXCLUDES_PROPERTY, arguments)

                            doProcessEntity(jsonDelegate,  incs, excs)

                        }
                        else {

                            StreamingJsonBuilder builder = new StreamingJsonBuilder(out)
                            builder.call {
                                StreamingJsonDelegate jsonDelegate = (StreamingJsonDelegate) getDelegate()
                                if (beforeClosure != null) {
                                    beforeClosure.setDelegate(jsonDelegate)
                                    beforeClosure.call(object)
                                }
                                List<String> incs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.INCLUDES_PROPERTY, arguments, null)
                                List<String> excs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.EXCLUDES_PROPERTY, arguments)

                                doProcessEntity(jsonDelegate,  incs, excs)
                            }
                        }

                    } else {
                        if(inline) {
                            StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate = new StreamingJsonBuilder.StreamingJsonDelegate(out, first)
                            if (beforeClosure != null) {
                                beforeClosure.setDelegate(jsonDelegate)
                                beforeClosure.call(object)
                            }
                            List<String> incs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.INCLUDES_PROPERTY, arguments, null)
                            List<String> excs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.EXCLUDES_PROPERTY, arguments)
                            doProcessSimple(jsonDelegate, incs, excs)
                        }
                        else {

                            StreamingJsonBuilder builder = new StreamingJsonBuilder(out)
                            builder.call {
                                StreamingJsonDelegate jsonDelegate = (StreamingJsonDelegate) getDelegate()
                                if (beforeClosure != null) {
                                    beforeClosure.setDelegate(jsonDelegate)
                                    beforeClosure.call(object)
                                }
                                List<String> incs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.INCLUDES_PROPERTY, arguments, null)
                                List<String> excs = ViewUtils.getStringListFromMap(IncludeExcludeSupport.EXCLUDES_PROPERTY, arguments)

                                doProcessSimple(jsonDelegate, incs, excs)
                            }
                        }
                    }


                    processedObjects.put(object, this)
                    return out
                } finally {

                    if (rootRender) {
                        binding.variables.remove(PROCESSED_OBJECT_VARIABLE)
                    }
                }
            }

        }

        return jsonWritable
    }


    @Override
    JsonOutput.JsonWritable render(Object object, Map arguments = Collections.emptyMap(), @DelegatesTo(StreamingJsonDelegate) Closure customizer = null ) {

        JsonView jsonView = (JsonView)view
        def binding = jsonView.getBinding()
        Map<Object, JsonOutput.JsonWritable> processedObjects = initializeProcessedObjects(binding)

        if(object instanceof Iterable) {
            return new JsonOutput.JsonWritable() {

                @Override
                Writer writeTo(Writer out) throws IOException {

                    Iterable iterable = (Iterable)object
                    int size = iterable.size()
                    int i = 0
                    out.append JsonOutput.OPEN_BRACKET
                    for(o in iterable) {
                        JsonOutput.JsonWritable writable = renderTemplate(o, arguments, customizer, processedObjects)
                        writable.writeTo(out)
                        if(++i != size) {
                            out.append JsonOutput.COMMA
                        }
                    }
                    out.append JsonOutput.CLOSE_BRACKET
                }
            }
        }
        else {

            return renderDefault(object, arguments, customizer, processedObjects)
        }
    }

    protected Map<Object, JsonOutput.JsonWritable> initializeProcessedObjects(Binding binding) {
        Map<Object, JsonOutput.JsonWritable> processedObjects

        if (binding.hasVariable(PROCESSED_OBJECT_VARIABLE)) {
            processedObjects = (Map<Object, JsonOutput.JsonWritable>) binding.getVariable(PROCESSED_OBJECT_VARIABLE)
        } else {
            processedObjects = new LinkedHashMap<Object, JsonOutput.JsonWritable>()
            binding.setVariable(PROCESSED_OBJECT_VARIABLE, processedObjects)
        }
        processedObjects
    }

    protected PersistentEntity findEntity(Object object) {
        def clazz = object.getClass()
        try {
            return GormEnhancer.findEntity(clazz)
        } catch (Throwable e) {
            return ((JsonView)view)?.mappingContext?.getPersistentEntity(clazz.name)
        }
    }

    protected void processSimple(StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate, Object object, Map<Object, JsonOutput.JsonWritable> processedObjects, List<String> incs, List<String> excs, String path, Closure customizer = null) {

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
                                        JsonOutput.JsonWritable jsonWritable = renderChildTemplate(childTemplate, propertyType, value)
                                        jsonDelegate.call(propertyName, jsonWritable)
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

            if (customizer != null) {
                customizer.setDelegate(jsonDelegate)
                customizer.call()
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

    protected void process(StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate, PersistentEntity entity, Object object, Map<Object, JsonOutput.JsonWritable> processedObjects, List<String> incs, List<String> excs, String path, boolean isDeep, List<String> expandProperties = [], boolean includeAssociations = true, Closure customizer = null) {

/*
        if(processedObjects.containsKey(object)) {
            return
        }
        processedObjects.put(object, NULL_OUTPUT)
*/


        def identity = entity.identity
        def idName = identity?.name
        String idQualified = "${path}${idName}"
        ResolvableGroovyTemplateEngine templateEngine = view.templateEngine
        Locale locale = view.locale
        if(idName != null && includeExcludeSupport.shouldInclude(incs, excs, idQualified)) {
            def idValue = ((GroovyObject) object).getProperty(idName)
            if(idValue != null) {
                def idType = identity.type
                def childTemplate = templateEngine?.resolveTemplate(idType, locale)
                if(childTemplate != null) {
                    JsonOutput.JsonWritable jsonWritable = renderChildTemplate(childTemplate, idType, idValue)
                    jsonDelegate.call(idName, jsonWritable)
                }
                else {
                    jsonDelegate.call(idName, idValue)
                }
            }
        }

        for (prop in entity.persistentProperties) {
            def propertyName = prop.name
            String qualified = "${path}${propertyName}"
            if (!includeExcludeSupport.shouldInclude(incs, excs, qualified)) continue

            def value = ((GroovyObject) object).getProperty(propertyName)
            if(value == null) continue

            if (!(prop instanceof Association)) {
                processSimpleProperty(jsonDelegate, (PersistentProperty) prop, propertyName, value, templateEngine, locale)
            } else if(includeAssociations) {
                Association ass = (Association) prop
                def associatedEntity = ass.associatedEntity

                if (ass instanceof Embedded) {
                    def propertyType = ass.type
                    def childTemplate = templateEngine?.resolveTemplate(propertyType, locale)
                    if(childTemplate != null) {
                        JsonOutput.JsonWritable jsonWritable = renderChildTemplate(childTemplate, propertyType, value)
                        jsonDelegate.call(propertyName, jsonWritable)
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

                        if(isDeep || expandProperties.contains(propertyName)/* || proxyHandler?.isInitialized(value)*/) {
                            def childTemplate = templateEngine?.resolveTemplate(TemplateResolverUtils.shortTemplateNameForClass(propertyType), locale)
                            if(childTemplate != null) {
                                def model = [(GrailsNameUtils.getPropertyName(propertyType)): value]
                                def childView = prepareWritable(childTemplate, model)
                                def writer = new FastStringWriter()
                                childView.writeTo(writer)
                                jsonDelegate.call(propertyName, JsonOutput.unescaped(writer.toString()))
                            }
                            else {
                                jsonDelegate.call(propertyName) {
                                    StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                    process(embeddedDelegate, associatedEntity,value, processedObjects, incs, excs , "${qualified}.", isDeep, expandProperties)
                                }
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
                        boolean shouldExpand = expandProperties.contains(propertyName)
                        if(!isDeep && !shouldExpand) {
                            def proxyHandler = ((JsonView) view).getProxyHandler()
                            if(proxyHandler?.isProxy(value) && !proxyHandler.isInitialized(value)) {
                                continue
                            }
                            if(value instanceof PersistentCollection) {
                                PersistentCollection pc = (PersistentCollection)value
                                if(!pc.isInitialized()) continue
                            }
                        }

                        if(isDeep || shouldExpand) {
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
                            } else {
                                jsonDelegate.call(propertyName, (Iterable)value) { child ->
                                    StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                    process(embeddedDelegate, associatedEntity,child, processedObjects, incs, excs , "${qualified}.", isDeep)
                                }
                            }
                        } else {
                            def associationIdName = associatedEntity.identity.name
                            jsonDelegate.call(propertyName, (Iterable)value) { child ->
                                def associatedId = ((GroovyObject)child).getProperty(associationIdName)
                                if(associatedId != null) {
                                    call(associationIdName, associatedId)
                                }
                                else {
                                    StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                                    process(embeddedDelegate, associatedEntity,child, processedObjects, incs, excs , "${qualified}.", isDeep, expandProperties)
                                }
                            }
                        }
                    }
                }
                else if(ass instanceof EmbeddedCollection) {
                    if(Iterable.isAssignableFrom(ass.type) && associatedEntity != null) {
                        jsonDelegate.call(propertyName, (Iterable)value) { child ->
                            StreamingJsonBuilder.StreamingJsonDelegate embeddedDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                            process(embeddedDelegate, associatedEntity,child, processedObjects, incs, excs , "${qualified}.", isDeep, expandProperties)
                        }
                    }
                }

            }
        }

        if (customizer != null) {
            customizer.setDelegate(jsonDelegate)
            customizer.call()
        }

    }

    protected void processSimpleProperty(StreamingJsonDelegate jsonDelegate, PersistentProperty prop, String propertyName, Object value, ResolvableGroovyTemplateEngine templateEngine, Locale locale) {
        if (prop instanceof Custom) {
            def propertyType = value.getClass()
            def childTemplate = templateEngine.resolveTemplate(propertyType, locale)
            if (childTemplate != null) {
                JsonOutput.JsonWritable jsonWritable = renderChildTemplate(childTemplate, propertyType, value)
                jsonDelegate.call(propertyName, jsonWritable)
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

    JsonOutput.JsonWritable renderChildTemplate(Template template, Class modelType, modelValue) {
        def childView = prepareWritable(template, [(GrailsNameUtils.getPropertyName(modelType)): modelValue])
        return new JsonOutput.JsonWritable() {

            @Override
            Writer writeTo(Writer out) throws IOException {
                childView.writeTo(out)
                return out
            }
        }

    }

    @Override
    JsonOutput.JsonWritable render(Map arguments) {
        def template = arguments.template

        def templateEngine = view.templateEngine
        if(template) {
            Map model = (Map)arguments.model ?: [:]
            def collection = arguments.collection
            def var = arguments.var ?: 'it'
            def templateUri = templateEngine
                    .viewUriResolver
                    .resolveTemplateUri(view.getControllerNamespace(), view.getControllerName(), template.toString())
            def childTemplate = templateEngine.resolveTemplate(templateUri, view.locale)
            if(childTemplate != null) {
                return new JsonOutput.JsonWritable() {

                    @Override
                    Writer writeTo(Writer out) throws IOException {
                        if(collection instanceof Iterable) {
                            Iterable iterable = (Iterable)collection
                            int size = iterable.size()
                            int i = 0
                            out.append JsonOutput.OPEN_BRACKET
                            for(o in collection) {
                                model.put(var, o)
                                def writable = prepareWritable(childTemplate, model)
                                writable.writeTo( out )
                                if(++i != size) {
                                    out.append JsonOutput.COMMA
                                }
                            }
                            out.append JsonOutput.CLOSE_BRACKET
                        }
                        else {
                            GrailsView writable = prepareWritable(childTemplate, model)
                            writable.writeTo( out )
                        }
                    }
                }

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
        writable.controllerNamespace = view.controllerNamespace
        writable.controllerName = view.controllerName
        writable.actionName = view.actionName
        return writable
    }
}
