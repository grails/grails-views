package grails.views.api.internal

import grails.views.api.GrailsView
import grails.views.api.GrailsViewHelper
import grails.web.mapping.LinkGenerator
import groovy.transform.CompileStatic
/**
 * Default methods for views, additional methods can be added via traits
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class DefaultGrailsViewHelper implements GrailsViewHelper {

    final GrailsView view


    DefaultGrailsViewHelper(GrailsView view) {
        this.view = view
    }

    LinkGenerator getLinkGenerator() {
        return view.linkGenerator
    }

    @Override
    String resource(Map params) {
        getLinkGenerator().resource(params)
    }

    @Override
    String link(Map params) {
        getLinkGenerator().link(params)
    }

    @Override
    String link(Map params, String encoding) {
        getLinkGenerator().link(params, encoding)
    }

    @Override
    String getContextPath() {
        getLinkGenerator().contextPath
    }

    @Override
    String getServerBaseURL() {
        getLinkGenerator().serverBaseURL
    }

    @Override
    String message(Map<String, Object> arguments) {
        def args = arguments.args
        def code = arguments.code?.toString()
        def defaultMessage = arguments.default?.toString() ?: code
        if(code != null) {
            if(args != null) {
                if(args instanceof List) {
                    args = ((List)args).toArray()
                }
                return view.messageSource.getMessage( code, (Object[])args, defaultMessage, view.locale)
            }
            else {
                return view.messageSource.getMessage( code, null, defaultMessage, view.locale)
            }
        }
        return defaultMessage
    }
}
