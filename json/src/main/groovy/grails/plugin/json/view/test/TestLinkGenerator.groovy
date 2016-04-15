package grails.plugin.json.view.test

import grails.web.mapping.LinkGenerator
import groovy.transform.CompileStatic
import org.grails.web.util.WebUtils

/**
 * A test link generator
 *
 * @author Graeme Rocher
 * @since 1.1
 */
@CompileStatic
class TestLinkGenerator implements LinkGenerator{
    @Override
    String resource(Map params) {
        return null
    }

    @Override
    String link(Map params, String encoding = "UTF-8") {
        final boolean absolute = params.absolute ? true : false
        final String base = absolute ? "${serverBaseURL}${contextPath}" : contextPath
        StringBuilder url = new StringBuilder(base)
        if(params.uri) {
            url.append(params.uri.toString())
        }
        else {
            Map urlObject = params
            if(params.url instanceof Map) {
                urlObject = (Map)params.url
            }

            String controller = urlObject.resource ?: urlObject.controller
            if(controller) {
                url.append("/$controller")

                if(urlObject.action) {
                    url.append("/$urlObject.action")
                    if(urlObject.id) {
                        url.append("/$urlObject.id")
                    }
                }
            }
        }
        if(params.params instanceof Map) {
            url.append WebUtils.toQueryString((Map)params.params, encoding)
        }
        return url.toString()
    }

    @Override
    String getContextPath() {
        return ''
    }

    @Override
    String getServerBaseURL() {
        return "http://localhost:8080"
    }
}
