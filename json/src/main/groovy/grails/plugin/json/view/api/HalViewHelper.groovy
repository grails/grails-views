package grails.plugin.json.view.api

import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.builder.StreamingJsonBuilder.StreamingJsonDelegate
import grails.rest.Link
import grails.views.api.GrailsViewHelper
import grails.views.api.HttpView
import grails.web.mime.MimeType
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod

/**
 * Helps creating HAL links
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class HalViewHelper {

    public static final String LINKS_ATTRIBUTE = "_links"
    public static final String SELF_ATTRIBUTE = "self"
    public static final String HREF_ATTRIBUTE = "href"
    public static final char COMMA = ','
    public static final String HREFLANG_ATTRIBUTE = "hreflang"
    public static final String TYPE_ATTRIBUTE = "type"
    public static final String EMBEDDED_ATTRIBUTE = "_embedded"

    JsonView view
    GrailsJsonViewHelper viewHelper
    String contentType = MimeType.HAL_JSON.name

    HalViewHelper(JsonView view, GrailsJsonViewHelper viewHelper) {
        this.view = view
        this.viewHelper = viewHelper
    }

    /**
     * Same as {@link GrailsJsonViewHelper#render(java.lang.Object, java.util.Map, groovy.lang.Closure)} but renders HAL links too
     */
    JsonOutput.JsonUnescaped render(Object object, Map arguments, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer) {
        arguments.put("beforeClosure", createlinksRenderingClosure(object))
        viewHelper.render(object, arguments, customizer)
    }


    JsonOutput.JsonUnescaped render(Object object, Map arguments) {
        arguments.put("beforeClosure", createlinksRenderingClosure(object))
        viewHelper.render(object, arguments, null)
    }

    /**
     * Same as {@link GrailsJsonViewHelper#render(java.lang.Object, java.util.Map, groovy.lang.Closure)} but renders HAL links too
     */
    JsonOutput.JsonUnescaped render(Object object,  @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer ) {
        viewHelper.render(object, [beforeClosure:createlinksRenderingClosure(object)], customizer)
    }

    JsonOutput.JsonUnescaped render(Object object) {
        viewHelper.render(object, [beforeClosure:createlinksRenderingClosure(object)], null)
    }

    /**
     * @param name Sets the HAL response type
     */
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
    void links(Object object, String contentType = this.contentType) {
        def locale = view.locale ?: Locale.ENGLISH
        contentType = view.mimeUtility?.getMimeTypeForExtension(contentType) ?: contentType
        new StreamingJsonDelegate(view.out, true).call(LINKS_ATTRIBUTE) {
            call(SELF_ATTRIBUTE) {
                call HREF_ATTRIBUTE, viewHelper.link(resource:object, method: HttpMethod.GET, absolute:true)
                call HREFLANG_ATTRIBUTE, locale.toString()

                call TYPE_ATTRIBUTE, contentType
            }

            Set<Link> links = getLinks(object)
            for(link in links) {
                call(link.rel) {
                    call HREF_ATTRIBUTE, link.href
                    call HREF_ATTRIBUTE, link.hreflang?.toString() ?: locale.toString()
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

    static class HalStreamingJsonDelegate extends StreamingJsonDelegate {
        String contentType
        HalViewHelper viewHelper
        StreamingJsonDelegate delegate

        HalStreamingJsonDelegate(HalViewHelper viewHelper, StreamingJsonDelegate delegate) {
            super(delegate.getWriter(), true)
            this.viewHelper = viewHelper
            this.delegate = delegate
            this.contentType = viewHelper.contentType
        }

        HalStreamingJsonDelegate(String contentType, HalViewHelper viewHelper, StreamingJsonDelegate delegate) {
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
            delegate.call(name, list)
        }

        @Override
        void call(String name, Object... array) throws IOException {
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
            delegate.call(name, value)
        }

        @Override
        void call(String name, JsonOutput.JsonUnescaped json) throws IOException {
            delegate.call(name, json)
        }
    }

    @CompileDynamic
    protected Closure<Void> createlinksRenderingClosure(object) {
        return {
            StreamingJsonDelegate local = (StreamingJsonDelegate) getDelegate()

            def previous = view.getOut()
            view.setOut(local.writer)
            try {
                links(object)
            } finally {
                view.setOut(previous)
            }
        }
    }
}
