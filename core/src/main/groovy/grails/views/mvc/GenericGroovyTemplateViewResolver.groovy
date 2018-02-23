package grails.views.mvc

import groovy.transform.CompileStatic
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.servlet.View
import org.springframework.web.servlet.ViewResolver

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
        if (webRequest != null) {
            def currentRequest = webRequest?.currentRequest
            if (viewName.startsWith('/')) {
                def controller = webRequest.controllerClass
                View view
                if (controller && controller.namespace) {
                    String namespacePrefix = "/" + controller.namespace
                    if (!viewName.startsWith(namespacePrefix)) {
                        view = smartViewResolver.resolveView(namespacePrefix + viewName, currentRequest, webRequest.response)
                    }
                }
                if (view == null) {
                    view = smartViewResolver.resolveView(viewName, currentRequest, webRequest.response)
                }
                return view
            } else {
                def controllerUri = webRequest?.attributes?.getControllerUri(currentRequest)
                View view = this.resolveViewWithController(controllerUri, viewName, webRequest)

                if (!view) {
                    controllerUri = currentRequest?.requestURI
                    view = this.resolveViewWithController(controllerUri, viewName, webRequest)
                }

                if (view) {
                    return view
                } else {
                    return smartViewResolver.resolveView(viewName, currentRequest, webRequest.response)
                }
            }
        } else {
            smartViewResolver.resolveView(viewName, locale)
        }
    }

    private View resolveViewWithController(String controllerUri, String viewName, GrailsWebRequest webRequest) {
        HttpServletRequest currentRequest = webRequest?.currentRequest
        HttpServletResponse currentResponse = webRequest?.currentResponse

        if (controllerUri) {
            return smartViewResolver.resolveView("${controllerUri}/$viewName", currentRequest, currentResponse)
        }
    }
}
