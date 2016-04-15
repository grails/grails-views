package grails.plugin.json.view.test

import grails.plugin.json.view.api.JsonView
import grails.views.api.HttpView
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod

/**
 * Allows configuring the JSON view rendering for request state
 *
 * @author Graeme Rocher
 * @since 1.1
 */
@CompileStatic
class TestRequestConfigurer implements HttpView.Request{

    final JsonView jsonView

    String contextPath
    String method = "GET"
    String uri
    String contentType = 'application/json'
    String characterEncoding = 'UTF-8'

    TestRequestConfigurer(JsonView jsonView) {
        this.jsonView = jsonView
        if(jsonView instanceof HttpView) {
            ((HttpView)jsonView).setRequest(this)
        }
    }

    void setActionName(String actionName) {
        jsonView.setActionName(actionName)
    }

    void setControllerName(String controllerName) {
        jsonView.setControllerName(controllerName)
    }


    TestRequestConfigurer actionName(String actionName) {
        this.actionName = actionName
        return this
    }

    TestRequestConfigurer controllerName(String controllerName) {
        this.controllerName = controllerName
        return this
    }

    TestRequestConfigurer method(String method) {
        this.method = method
        return this
    }

    TestRequestConfigurer method(HttpMethod method) {
        this.method = method.toString()
        return this
    }

}
