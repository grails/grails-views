package grails.views.mvc

import grails.util.Environment
import grails.views.ResolvableGroovyTemplateEngine
import grails.web.mapping.LinkGenerator
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.view.AbstractUrlBasedView
import org.springframework.web.servlet.view.UrlBasedViewResolver

/**
 * A UrlBasedViewResolver for ResolvableGroovyTemplateEngine
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class GenericGroovyTemplateViewResolver extends UrlBasedViewResolver {

    @Autowired
    LinkGenerator linkGenerator

    @Autowired
    LocaleResolver localeResolver

    @Delegate final ResolvableGroovyTemplateEngine templateEngine

    GenericGroovyTemplateViewResolver(ResolvableGroovyTemplateEngine templateEngine) {
        this.templateEngine = templateEngine
        setCache( !Environment.isDevelopmentMode() )
        setViewClass(GenericGroovyTemplateView)
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        GenericGroovyTemplateView view = (GenericGroovyTemplateView)super.buildView(viewName)
        view.templateEngine = templateEngine
        view.linkGenerator = linkGenerator
        view.localeResolver = localeResolver
        return view
    }
}
