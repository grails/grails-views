package grails.plugin.json.view

import grails.views.mvc.GenericGroovyTemplateView
import org.springframework.web.servlet.View
import spock.lang.Specification

/**
 * Created by graemerocher on 24/08/15.
 */
class JsonViewTemplateResolverSpec extends Specification {

    void "Test that the template resolver works"() {
        given:"A viewResolver with a mock template resolver"
        def viewResolver = new JsonViewResolver() {
            @Override
            protected View loadView(String viewName, Locale locale) throws Exception {
                return this.buildView(viewName)
            }
        }

        when:"We resolve a template"
        GenericGroovyTemplateView view = (GenericGroovyTemplateView)viewResolver.resolveViewName("/foo/bar", Locale.ENGLISH)

        then:"The view is not null"
        view != null
        view.url == '/foo/bar.gson'
        view.templateEngine != null
        view.contentType == 'application/json'


    }
}
