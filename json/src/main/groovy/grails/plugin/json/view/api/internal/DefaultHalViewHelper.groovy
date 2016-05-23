package grails.plugin.json.view.api.internal

import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.builder.StreamingJsonBuilder.StreamingJsonDelegate
import grails.plugin.json.view.api.GrailsJsonViewHelper
import grails.plugin.json.view.api.HalViewHelper
import grails.plugin.json.view.api.JsonView
import grails.rest.Link
import grails.views.api.HttpView
import grails.views.utils.ViewUtils
import grails.web.mime.MimeType
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.core.util.IncludeExcludeSupport
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Basic
import org.grails.datastore.mapping.model.types.Custom
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.datastore.mapping.model.types.EmbeddedCollection
import org.grails.datastore.mapping.model.types.Simple
import org.grails.datastore.mapping.model.types.ToMany
import org.grails.datastore.mapping.model.types.ToOne
import org.grails.datastore.mapping.proxy.ProxyHandler
import org.grails.datastore.mapping.reflect.ClassUtils
import org.grails.datastore.mapping.reflect.EntityReflector
import org.springframework.http.HttpMethod

/**
 * Helps creating HAL links
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class DefaultHalViewHelper implements HalViewHelper {

    public static final String LINKS_ATTRIBUTE = "_links"
    public static final String SELF_ATTRIBUTE = "self"
    public static final String HREF_ATTRIBUTE = "href"
    public static final char COMMA = ','
    public static final String HREFLANG_ATTRIBUTE = "hreflang"
    public static final String TYPE_ATTRIBUTE = "type"
    public static final String EMBEDDED_ATTRIBUTE = "_embedded"
    public static final String EMBEDDED_PARAMETER = "embedded"

    JsonView view
    GrailsJsonViewHelper viewHelper
    String contentType = MimeType.HAL_JSON.name

    DefaultHalViewHelper(JsonView view, GrailsJsonViewHelper viewHelper) {
        this.view = view
        this.viewHelper = viewHelper
    }

    /**
     * Same as {@link GrailsJsonViewHelper#render(java.lang.Object, java.util.Map, groovy.lang.Closure)} but renders HAL links too
     */
    @Override
    JsonOutput.JsonUnescaped render(Object object, Map arguments, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer) {
        if(arguments == null) {
            arguments = new LinkedHashMap()
        }

        if(!arguments.containsKey("beforeClosure")) {
            arguments.put("beforeClosure", createlinksRenderingClosure(object, arguments))
        }

        viewHelper.render(object, arguments, customizer)
    }


    @Override
    JsonOutput.JsonUnescaped render(Object object, Map arguments) {
        render(object, arguments, null)
    }

    /**
     * Same as {@link GrailsJsonViewHelper#render(java.lang.Object, java.util.Map, groovy.lang.Closure)} but renders HAL links too
     */
    @Override
    JsonOutput.JsonUnescaped render(Object object,  @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer ) {
        render(object, (Map)null, customizer)
    }

    @Override
    JsonOutput.JsonUnescaped render(Object object) {
        render(object, (Map)null, (Closure)null)
    }

    @Override
    void inline(Object object, Map arguments = [:], @DelegatesTo(StreamingJsonDelegate) Closure customizer = null) {
        arguments.put(GrailsJsonViewHelper.ASSOCIATIONS, false)
        viewHelper.inline(object, arguments, customizer)
    }


    @Override
    void inline(Object object, @DelegatesTo(StreamingJsonDelegate) Closure customizer) {
        inline(object, [:], customizer)
    }

    /**
     * @param name Sets the HAL response type
     */
    @Override
    void type(String name) {
        def mimeType = view.mimeUtility?.getMimeTypeForExtension(name)
        this.contentType = mimeType?.name ?: name
        if(view instanceof HttpView) {
            ((HttpView)view).response?.contentType(contentType)
        }
    }

    /**
     * Define the hal links
     *
     * @param callable The closure
     */
    @Override
    void links(Closure callable) {
        new StreamingJsonDelegate(view.out, true).call(LINKS_ATTRIBUTE) {

            callable.setDelegate(new HalStreamingJsonDelegate(this, (StreamingJsonDelegate)delegate))
            callable.call()
        }
        view.out.write(JsonOutput.COMMA)

    }
    /**
     * Creates HAL links for the given object
     *
     * @param object The object to create links for
     */
    void links(Object object, String contentType = this.contentType, boolean writeComma = true) {
        def jsonDelegate = new StreamingJsonDelegate(view.out, true)
        writeLinks(jsonDelegate, object, contentType, writeComma)
    }

    /**
     * Pagination support which outputs hal links to the resulting pages
     *
     * @param object The object to create links for
     * @param total The total number of objects to be paginated
     * @param offset The numerical offset where the page starts (defaults to 0)
     * @param max The maximum number of objects to be shown (defaults to 10)
     * @param sort The field to sort on (defaults to null)
     * @param order The order in which the results are to be sorted eg: DESC or ASC
     */
    void paginate(Object object, Integer total, Integer offset = null, Integer max = null,  String sort = null, String order = null) {
        Map<String, Object> linkParams = buildPaginateParams(max, offset, sort, order)

        def httpParams = view.params
        offset = offset ?: httpParams.int('offset', 0)
        max = max ?: httpParams.int('max', 10)
        sort = sort ?: httpParams.get('sort')
        order = order ?: httpParams.get('order')

        String contentType = this.contentType
        def locale = view.locale ?: Locale.ENGLISH
        contentType = view.mimeUtility?.getMimeTypeForExtension(contentType) ?: contentType
        new StreamingJsonDelegate(view.out, true).call(LINKS_ATTRIBUTE) {
            call(SELF_ATTRIBUTE) {
                call HREF_ATTRIBUTE, viewHelper.link(resource:object, method: HttpMethod.GET, absolute:true, params: linkParams)  //TODO handle the max/offset here
                call HREFLANG_ATTRIBUTE, locale.toString()

                call TYPE_ATTRIBUTE, contentType
            }
            List<Link> links = getPaginationLinks(object, total, max, offset, sort, order)
            for(link in links) {
                call(link.rel) {
                    call HREF_ATTRIBUTE, link.href
                    call HREFLANG_ATTRIBUTE, link.hreflang?.toString() ?: locale.toString()
                    def linkType = link.contentType
                    if(linkType) {
                        call TYPE_ATTRIBUTE, linkType
                    }
                }
            }

        }
        view.out.write(JsonOutput.COMMA)
    }

    /**
     * Renders embedded associations for the given entity
     *
     * @param object The object
     * @param arguments Any arguments. Supported arguments: 'includes', 'excludes', 'deep'
     */
    void embedded(Object object, Map arguments = [:]) {

        MappingContext mappingContext = view.mappingContext
        def proxyHandler = mappingContext.proxyHandler
        object = proxyHandler != null ? proxyHandler.unwrap(object) : object
        PersistentEntity entity = mappingContext.getPersistentEntity(object.getClass().name)

        List<String> incs = (List<String>)arguments.get(IncludeExcludeSupport.INCLUDES_PROPERTY) ?: null
        List<String> excs = (List<String>)arguments.get(IncludeExcludeSupport.EXCLUDES_PROPERTY) ?: new ArrayList<String>()
        List<String> expandProperties = (List<String>)(view.params.list(GrailsJsonViewHelper.EXPAND) ?: arguments.get(GrailsJsonViewHelper.EXPAND) ?: Collections.emptyList())
        boolean deep = ViewUtils.getBooleanFromMap(GrailsJsonViewHelper.DEEP, arguments)
        arguments.put(IncludeExcludeSupport.EXCLUDES_PROPERTY, excs)
        def includeExcludeSupport = ((DefaultGrailsJsonViewHelper) viewHelper).includeExcludeSupport
        if(entity != null) {
            EntityReflector entityReflector = mappingContext.getEntityReflector(entity)
            def associations = entity.associations
            Map<Association, Object> embeddedValues = [:]
            for(Association association in associations) {
                if(!association.isEmbedded()) {
                    def propertyName = association.name
                    if (includeExcludeSupport.shouldInclude(incs, excs, propertyName)) {
                        def value = entityReflector.getProperty(object, propertyName)
                        if(value != null) {

                            if(association instanceof ToMany && !( association instanceof Basic)) {
                                if(deep || expandProperties.contains(propertyName) || proxyHandler == null || proxyHandler.isInitialized(value)) {
                                    embeddedValues.put(association, value)
                                }
                                excs.add(propertyName)
                            }
                            else if(association instanceof ToOne) {
                                if(deep || expandProperties.contains(propertyName) || proxyHandler == null || proxyHandler.isInitialized(value)) {
                                    embeddedValues.put(association, value)
                                }
                                excs.add(propertyName)
                            }
                        }
                    }
                }
            }

            if(!embeddedValues.isEmpty()) {
                embedded {
                    StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                    for(entry in embeddedValues) {
                        def embeddedObject = entry.value
                        Association association = entry.key
                        def associatedEntity = association.associatedEntity
                        def associationReflector = mappingContext.getEntityReflector(associatedEntity)
                        def propertyName = association.name
                        if(association instanceof ToOne) {
                            jsonDelegate.call(propertyName) {
                                def associationJsonDelegate = (StreamingJsonDelegate) getDelegate()
                                writeLinks(associationJsonDelegate, embeddedObject, this.contentType)
                                renderEntityProperties(associatedEntity, embeddedObject, associationReflector, associationJsonDelegate)
                            }

                        }
                        else if(association instanceof ToMany) {
                            if(embeddedObject instanceof Iterable) {
                                jsonDelegate.call(propertyName, (Iterable)embeddedObject) { e ->
                                    def associationJsonDelegate = (StreamingJsonDelegate) getDelegate()
//                                    writeLinks(associationJsonDelegate, e, this.contentType)
                                    renderEntityProperties(associatedEntity, e, associationReflector, associationJsonDelegate)
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Render the given embedded objects
     *
     * @param model The embedded model
     */
    @Override
    void embedded(Map model) {
        if(!model?.isEmpty()) {
            MappingContext mappingContext = view.mappingContext
            def proxyHandler = mappingContext.proxyFactory

            embedded {
                StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate = (StreamingJsonBuilder.StreamingJsonDelegate)getDelegate()
                for(entry in model.entrySet()) {
                    Object embeddedObject = proxyHandler.unwrap( entry.value )

                    if(Iterable.isInstance(embeddedObject)) {
                        jsonDelegate.call(entry.key.toString(), (Iterable)embeddedObject) { e ->
                            PersistentEntity entity = mappingContext.getPersistentEntity(e.getClass().name)
                            def entityReflector = mappingContext.getEntityReflector(entity)
                            def associationJsonDelegate = (StreamingJsonDelegate) getDelegate()
                            writeLinks(associationJsonDelegate, e, this.contentType)
                            renderEntityProperties(entity, e, entityReflector, associationJsonDelegate)
                        }
                    }
                    else {
                        PersistentEntity entity = mappingContext.getPersistentEntity(embeddedObject.getClass().name)
                        if(entity != null) {
                            def entityReflector = mappingContext.getEntityReflector(entity)
                            jsonDelegate.call(entry.key.toString()) {
                                if(entity != null) {
                                    def associationJsonDelegate = (StreamingJsonDelegate) getDelegate()
                                    writeLinks(associationJsonDelegate, embeddedObject, this.contentType)
                                    renderEntityProperties(entity, embeddedObject, entityReflector, associationJsonDelegate)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void writeLinks(StreamingJsonDelegate jsonDelegate, object, String contentType, boolean writeComma = false) {
        def locale = view.locale ?: Locale.ENGLISH
        contentType = view.mimeUtility?.getMimeTypeForExtension(contentType) ?: contentType
        jsonDelegate.call(LINKS_ATTRIBUTE) {
            call(SELF_ATTRIBUTE) {
                call HREF_ATTRIBUTE, viewHelper.link(resource: object, method: HttpMethod.GET, absolute: true)
                call HREFLANG_ATTRIBUTE, locale.toString()

                call TYPE_ATTRIBUTE, contentType
            }

            Set<Link> links = getLinks(object)
            for (link in links) {
                call(link.rel) {
                    call HREF_ATTRIBUTE, link.href
                    call HREFLANG_ATTRIBUTE, link.hreflang?.toString() ?: locale.toString()
                    def linkType = link.contentType
                    if (linkType) {
                        call TYPE_ATTRIBUTE, linkType
                    }
                }
            }

        }
        if (writeComma) {
            view.out.write(JsonOutput.COMMA)
        }
    }

    protected void renderEntityProperties(PersistentEntity entity, Object instance, EntityReflector entityReflector, StreamingJsonDelegate associationJsonDelegate) {
        for (prop in entity.persistentProperties) {
            renderProperty(instance, prop, entityReflector, associationJsonDelegate)
        }
    }

    protected void renderProperty(Object embeddedObject, PersistentProperty prop,  EntityReflector associationReflector, StreamingJsonBuilder.StreamingJsonDelegate jsonDelegate) {
        def propertyName = prop.name
        def propVal = associationReflector.getProperty(embeddedObject, propertyName)
        def propertyType = prop.type
        if (propVal != null) {
            if ((prop instanceof Simple) || (prop instanceof Basic)) {
                if (DefaultGrailsJsonViewHelper.isStringType(propertyType)) {
                    jsonDelegate.call(propertyName, propVal.toString())
                } else {
                    jsonDelegate.call(propertyName, propVal)
                }
            } else if (prop instanceof Custom) {
                def childTemplate = view.templateEngine.resolveTemplate(propertyType, view.locale)
                if (childTemplate != null) {
                    JsonOutput.JsonUnescaped jsonUnescaped = ((DefaultGrailsJsonViewHelper) viewHelper).renderChildTemplate(childTemplate, propertyType, propVal)
                    jsonDelegate.call(propertyName, jsonUnescaped)
                } else {
                    jsonDelegate.call(propertyName, propVal)
                }
            } else if(prop instanceof Embedded) {
                Embedded embedded = (Embedded)prop
                def childTemplate = view.templateEngine.resolveTemplate(propertyType, view.locale)
                if (childTemplate != null) {
                    JsonOutput.JsonUnescaped jsonUnescaped = ((DefaultGrailsJsonViewHelper) viewHelper).renderChildTemplate(childTemplate, propertyType, propVal)
                    jsonDelegate.call(propertyName, jsonUnescaped)
                } else {
                    jsonDelegate.call(propertyName) {
                        def associationJsonDelegate = (StreamingJsonDelegate) getDelegate()
                        def associatedEntity = embedded.associatedEntity
                        def embeddedReflector = associatedEntity.getMappingContext().getEntityReflector(associatedEntity)
                        renderEntityProperties(associatedEntity, propVal, embeddedReflector , associationJsonDelegate)
                    }
                }
            } else if(prop instanceof EmbeddedCollection) {
                EmbeddedCollection embedded = (EmbeddedCollection)prop
                def associatedEntity = embedded.associatedEntity
                def associatedType = associatedEntity.javaClass
                def embeddedReflector = associatedEntity.getMappingContext().getEntityReflector(associatedEntity)
                def childTemplate = view.templateEngine.resolveTemplate(associatedType, view.locale)
                if (childTemplate != null) {
                    if(propVal instanceof Iterable) {
                        def childResults = ((Iterable) propVal).collect() { eo ->
                            ((DefaultGrailsJsonViewHelper) viewHelper).renderChildTemplate(childTemplate, associatedType, eo)
                        }.toList()
                        jsonDelegate.call(embedded.name, (List<Object>) childResults)
                    }
                }
                else {
                    if(propVal instanceof Iterable) {
                        jsonDelegate.call(embedded.name, (Iterable)propVal) { eo ->
                            def associationJsonDelegate = (StreamingJsonDelegate) getDelegate()
                            renderEntityProperties(associatedEntity, eo, embeddedReflector , associationJsonDelegate)
                        }
                    }
                }
            }

        }
    }

    /**
     * Outputs a HAL embedded entry for the given closure
     *
     * @param callable The callable
     */
    void embedded(@DelegatesTo(StreamingJsonDelegate) Closure callable) {
        new StreamingJsonDelegate(view.out, true).call(EMBEDDED_ATTRIBUTE) {

            callable.setDelegate(new HalStreamingJsonDelegate(this, (StreamingJsonDelegate)delegate))
            callable.call()
        }
        view.out.write(JsonOutput.COMMA)
    }

    /**
     * Outputs a HAL embedded entry for the content type and closure
     *
     * @param callable The callable
     */
    void embedded(String contentType, @DelegatesTo(StreamingJsonDelegate) Closure callable) {
        new StreamingJsonDelegate(view.out, true).call(EMBEDDED_ATTRIBUTE) {

            callable.setDelegate(new HalStreamingJsonDelegate(contentType, this, (StreamingJsonDelegate)delegate))
            callable.call()
        }
        view.out.write(JsonOutput.COMMA)
    }

    @CompileDynamic
    private Set<Link> getLinks(Object o) {
        if(o.respondsTo("links")) {
            return o.links()
        }
        else {
            Collections.emptySet()
        }
    }

    /**
     * @return the pagination links if any
     */
    protected List<Link> getPaginationLinks(Object object, Integer total, Integer max, Integer offset, String sort, String order) {
        Map<String, Object> linkParams = buildPaginateParams(max, offset, sort, order)
        List<Link> links = []

        if (total > 0) {
            if (total > max) {
                Map firstParams = paramsWithOffset(linkParams, 0)
                links << new Link("first", viewHelper.link(resource: object, method: HttpMethod.GET, absolute: true, params: firstParams))
                Integer prevOffset = getPrevOffset(offset, max)
                if (prevOffset != null) {
                    Map prevParams = paramsWithOffset(linkParams, prevOffset)
                    links << new Link("prev", viewHelper.link(resource: object, method: HttpMethod.GET, absolute: true, params: prevParams))
                }
                Integer nextOffset = getNextOffset(total, offset, max)
                if (nextOffset) {
                    Map nextParams = paramsWithOffset(linkParams, nextOffset)
                    links << new Link("next", viewHelper.link(resource: object, method: HttpMethod.GET, absolute: true, params: nextParams))
                }
                Integer lastOffset = getLastOffset(total, max)
                if (lastOffset) {
                    Map lastParams = paramsWithOffset(linkParams, lastOffset)
                    links << new Link("last", viewHelper.link(resource: object, method: HttpMethod.GET, absolute: true, params: lastParams))
                }
            }
        }
        return links
    }

    protected Map<String, Object> buildPaginateParams(Integer max, Integer offset, String sort, String order) {
        Map<String, Object> params = [:]
        params.offset = offset
        params.max = max
        if (sort) {
            params.sort = sort
        }
        if (order) {
            params.order = order
        }
        return params
    }

    /**
     * Creates a new Parameter map with the new offset
     * Note: neccesary to avoid clone until Groovy 2.5.x https://issues.apache.org/jira/browse/GROOVY-7325
     *
     * @param map The parameter map to copy
     * @param offset The new offset to use
     * @return The resulting parameters
     */
    protected Map<String, Object> paramsWithOffset(Map<String, Object> originalParameters, Integer offset) {
        Map<String, Object> params = [:]
        originalParameters.each { String k, Object v ->
            params.put(k, v)
        }
        params.offset = offset
        return params
    }

    protected Integer getPrevOffset(Integer offset, Integer max) {
        if (offset <= 0) {
            return null
        }
        return Math.max(offset - max, 0)
    }

    protected Integer getNextOffset(Integer total, Integer offset, Integer max) {
        if (offset < 0 || offset + max >= total) {
            return null
        }
        return offset + max
    }

    protected Integer getLastOffset(Integer total, Integer max) {
        if (total <= 0) {
            return null
        }
        Integer laststep = ((int) Math.round(Math.ceil(total / max))) - 1
        return Math.max((laststep * max), 0)
    }

    static class HalStreamingJsonDelegate extends StreamingJsonDelegate {
        String contentType
        DefaultHalViewHelper viewHelper
        StreamingJsonDelegate delegate

        HalStreamingJsonDelegate(DefaultHalViewHelper viewHelper, StreamingJsonDelegate delegate) {
            super(delegate.getWriter(), true)
            this.viewHelper = viewHelper
            this.delegate = delegate
            this.contentType = viewHelper.contentType
        }

        HalStreamingJsonDelegate(String contentType, DefaultHalViewHelper viewHelper, StreamingJsonDelegate delegate) {
            super(delegate.getWriter(), true)
            this.viewHelper = viewHelper
            this.delegate = delegate
            this.contentType = contentType
        }

        @Override
        Object invokeMethod(String name, Object args) {
            Object[] arr = (Object[]) args
                switch(arr.length) {
                    case 1:
                        final Object value = arr[0];
                        if(value instanceof Closure) {
                            call(name, (Closure)value);
                        }
                        else {
                            call(name, value);
                        }
                        return null;
                    case 2:
                        if(arr[-1] instanceof Closure) {
                            final Object obj = arr[0]
                            final Closure callable = (Closure) arr[1]
                            if(obj instanceof Iterable) {
                                call(name, (Iterable)obj, callable)
                                return null
                            }
                            else if(obj.getClass().isArray()) {
                                call(name, Arrays.asList( (Object[])obj), callable)
                                return null
                            }
                            else {
                                call(name, obj, callable)
                                return null
                            }
                        }
                    default:
                        return delegate.invokeMethod(name, args)
                }
        }

        @Override
        void call(String name, List<Object> list) throws IOException {
            first = false
            delegate.call(name, list)
        }

        @Override
        void call(String name, Object... array) throws IOException {
            first = false
            delegate.call(name, array)
        }

        @Override
        void call(String name, Iterable coll, Closure c) throws IOException {
            writeName(name)
            verifyValue();
            def w = writer
            w.write(JsonOutput.OPEN_BRACKET)
            boolean first = true
            for (Object it in coll) {
                if (!first) {
                    w.write(JsonOutput.COMMA)
                } else {
                    first = false
                }

                writeObject( it, c)
            }
            w.write(JsonOutput.CLOSE_BRACKET)

        }

        @Override
        void call(String name, Object value) throws IOException {
            first = false
            delegate.call(name, value)
        }

        @Override
        void call(String name, Object value,
                  @DelegatesTo(StreamingJsonDelegate.class) Closure callable) throws IOException {
            writeName(name)
            verifyValue()
            writeObject(value, callable)
        }

        protected void writeObject(value, Closure callable) {
            writer.write(JsonOutput.OPEN_BRACE)
            viewHelper.links(value, contentType)
            curryDelegateAndGetContent(writer, callable, value)
            writer.write(JsonOutput.CLOSE_BRACE)
        }

        @Override
        void call(String name, @DelegatesTo(StreamingJsonDelegate.class) Closure value) throws IOException {
            first = false
            delegate.call(name, value)
        }

        @Override
        void call(String name, JsonOutput.JsonUnescaped json) throws IOException {
            first = false
            delegate.call(name, json)
        }
    }

    @CompileDynamic
    protected Closure<Void> createlinksRenderingClosure(object, Map<String, Object> arguments = [:]) {
        return {
            StreamingJsonDelegate local = (StreamingJsonDelegate) getDelegate()

            def previous = view.getOut()
            view.setOut(local.writer)
            try {

                def shouldRenderEmbedded = ViewUtils.getBooleanFromMap(EMBEDDED_PARAMETER, arguments, true)
                if(shouldRenderEmbedded) {
                    embedded(object, arguments)
                }
                links(object)
            } finally {
                view.setOut(previous)
            }
        }
    }
}
