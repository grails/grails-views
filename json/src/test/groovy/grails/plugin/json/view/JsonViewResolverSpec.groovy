package grails.plugin.json.view

import grails.plugin.json.view.mvc.JsonViewResolver
import grails.views.GenericGroovyTemplateResolver
import grails.web.mapping.LinkGenerator
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.View
import spock.lang.Specification

/**
 * Created by graemerocher on 25/08/15.
 */
class JsonViewResolverSpec extends Specification {

    // We need Groovy 2.4.5 with extensible StreamingJsonBuilder to support templates
    void "Test render templates"() {
        given:"A view resolver"
        def resolver = new JsonViewResolver(){
            @Override
            protected View loadView(String viewName, Locale locale) throws Exception {
                return this.buildView(viewName)
            }
        }
        configureIfRunFromRoot(resolver)

        when:"A view is resolved"
        def view = resolver.resolveViewName("/parent", Locale.ENGLISH)
        def response = new MockHttpServletResponse()
        view.render([childList:[1,2], age:25], new MockHttpServletRequest(), response)

        then:"The page response header is set"
//        response.contentType == 'application/json'
        response.contentAsString == '{"name":"Joe","age":25,"child":{"name":"Fred","age":4},"children":[{"name":"Fred","age":1},{"name":"Fred","age":2},{"name":"Fred","age":3}]}'
    }

    void "Test create links using LinkGenerator"() {
        given:"A view resolver"
        def resolver = new JsonViewResolver(){
            @Override
            protected View loadView(String viewName, Locale locale) throws Exception {
                return this.buildView(viewName)
            }
        }

        configureIfRunFromRoot(resolver)

        def linkGenerator = Mock(LinkGenerator)
        linkGenerator.link(_) >> { args -> "http://foo.com/${args[0].controller}"}
        resolver.linkGenerator = linkGenerator

        when:"A view is resolved"
        def view = resolver.resolveViewName("/linkingView", Locale.ENGLISH)
        def response = new MockHttpServletResponse()
        view.render([:], new MockHttpServletRequest(), response)

        then:"The page response header is set"
        response.contentType == 'application/json'
        response.contentAsString == '{"person":{"name":"bob","homepage":"http://foo.com/person"}}'
    }


    void "Test that a resolved JSON view can configure the page response"() {
        given:"A view resolver"
        def resolver = new JsonViewResolver(){
            @Override
            protected View loadView(String viewName, Locale locale) throws Exception {
                return this.buildView(viewName)
            }
        }
        configureIfRunFromRoot(resolver)


        when:"A view is resolved"
        def view = resolver.resolveViewName("/pageConfigure", Locale.ENGLISH)
        def response = new MockHttpServletResponse()
        view.render([:], new MockHttpServletRequest(), response)

        then:"The page response header is set"
        response.getHeader("foo") == "bar"
        response.contentType == 'application/hal+json'
    }

    protected void configureIfRunFromRoot(JsonViewResolver resolver) {
        def parent = new File("./json/grails-app/views")
        if (parent.exists()) {
            resolver.setTemplateResolver(
                    new GenericGroovyTemplateResolver(baseDir: parent)
            )
        }
    }

}
