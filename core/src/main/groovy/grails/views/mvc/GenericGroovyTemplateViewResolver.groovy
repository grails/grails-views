package grails.views.mvc

import grails.util.Environment
import grails.views.ResolvableGroovyTemplateEngine
import grails.web.mapping.LinkGenerator
import grails.web.mime.MimeUtility
import groovy.transform.CompileStatic
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.AbstractUrlBasedView
import org.springframework.web.servlet.view.UrlBasedViewResolver

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.servlet.http.HttpServletRequest

/**
 * A UrlBasedViewResolver for ResolvableGroovyTemplateEngine
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class GenericGroovyTemplateViewResolver extends UrlBasedViewResolver implements Closeable {

    public static final String SLASH = "/"
    @Autowired
    LinkGenerator linkGenerator

    @Autowired
    LocaleResolver localeResolver

    @Autowired
    MimeUtility mimeUtility

    @Delegate final ResolvableGroovyTemplateEngine templateEngine

    GenericGroovyTemplateViewResolver(ResolvableGroovyTemplateEngine templateEngine) {
        this.templateEngine = templateEngine
        setCache( !Environment.isDevelopmentMode() )
        setViewClass(GenericGroovyTemplateView)
    }

    /**
     * Sets whether to reload changes to templates
     *
     * @param enableReloading Reload changes if set to true
     */
    void setEnableReloading(boolean enableReloading) {
        this.templateEngine.setEnableReloading(enableReloading)
    }

    /**
     * @param name The name of the package to use
     */
    void setPackageName(String name) {
        this.templateEngine.setPackageName(name)
    }

    @Override
    protected View loadView(String viewName, Locale locale) throws Exception {
        def view = (AbstractUrlBasedView)super.loadView(viewName, locale)

        return view
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        GenericGroovyTemplateView view = (GenericGroovyTemplateView)super.buildView(viewName)
        view.templateEngine = templateEngine
        view.linkGenerator = linkGenerator
        view.localeResolver = localeResolver
        view.mimeUtility = mimeUtility
        return view
    }

    @Override
    View resolveViewName(String viewName, Locale locale) throws Exception {
        if(viewName.startsWith(SLASH)) {
            return super.resolveViewName(viewName, locale)
        }
        else {
            def webRequest = GrailsWebRequest.lookup()
            def currentRequest = webRequest?.currentRequest
            def controllerUri = webRequest?.attributes?.getControllerUri(currentRequest)
            if(controllerUri) {
                return super.resolveViewName(
                        "${controllerUri}/$viewName",
                        locale
                )
            }
            else {
                return super.resolveViewName(viewName, locale)
            }

        }
    }

    @PreDestroy
    void close() {
        templateEngine.close()
    }
}
