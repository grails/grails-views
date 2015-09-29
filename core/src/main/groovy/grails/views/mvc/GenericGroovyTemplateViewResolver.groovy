package grails.views.mvc

import groovy.transform.CompileStatic
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.servlet.View
import org.springframework.web.servlet.ViewResolver
/**
 * A UrlBasedViewResolver for ResolvableGroovyTemplateEngine
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class GenericGroovyTemplateViewResolver implements ViewResolver {

    SmartViewResolver smartViewResolver

    GenericGroovyTemplateViewResolver(SmartViewResolver smartViewResolver) {
        this.smartViewResolver = smartViewResolver
    }

    @Override
    View resolveViewName(String viewName, Locale locale) throws Exception {
        def webRequest = GrailsWebRequest.lookup()
        if(webRequest != null) {
            def currentRequest = webRequest?.currentRequest
            if(viewName.startsWith('/')) {
                return smartViewResolver.resolveView(viewName, currentRequest, webRequest.response)
            }
            else {

                def controllerUri = webRequest?.attributes?.getControllerUri(currentRequest)
                if(controllerUri) {
                    return smartViewResolver.resolveView(
                            "${controllerUri}/$viewName",
                            currentRequest,
                            webRequest.currentResponse
                    )
                }
                else {
                    return smartViewResolver.resolveView(viewName, currentRequest, webRequest.response)
                }
            }
        }
        else {
            smartViewResolver.resolveView(viewName, locale)
        }
    }
}
