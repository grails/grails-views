package functional.tests.api

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.web.http.HttpHeaders
import spock.lang.Issue

@Integration
@Rollback
class NamespacedBookSpec extends GebSpec {

    void "test view rendering with a namespace"() {
        given: "A rest client"
            def builder = new RestBuilder()

        when: "A request is sent to a controller with a namespace"
            RestResponse resp = builder.get("${baseUrl}api/book")

        then: "The response is correct"
            resp.status == 200
            resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
            resp.json.api == "version 1.0 (Namespaced)"
            resp.json.title == "API - The Shining"
    }

    void "test nested template rendering with a namespace"() {
        given: "A rest client"
            def builder = new RestBuilder()

        when: "A request is sent to a controller with a namespace"
            RestResponse resp = builder.get("${baseUrl}api/book/nested")

        then: "The response contains the child template"
            resp.status == 200
            resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
            resp.json.foo == "bar"
    }

    void "test the correct content type is chosen (json)"() {
        given: "A rest client"
        def builder = new RestBuilder()

        when: "A request is sent to a controller with a namespace"
        RestResponse resp = builder.get("${baseUrl}api/book") {
            accept "application/json"
        }

        then: "The response contains the child template"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        !resp.json.containsKey("_links")
        resp.json.api == "version 1.0 (Namespaced)"
        resp.json.title == "API - The Shining"
    }

    void "test the correct content type is chosen (hal)"() {
        given: "A rest client"
        def builder = new RestBuilder()

        when: "A request is sent to a controller with a namespace"
        RestResponse resp = builder.get("${baseUrl}api/book") {
            accept "application/hal+json"
        }

        then: "The response contains the child template"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/hal+json;charset=UTF-8'
        resp.json.containsKey("_links")
        resp.json.api == "version 1.0 (Namespaced HAL)"
        resp.json.title == "API - The Shining"
    }

    void "test render(view: '..', model: ..) in controllers with namespaces works"() {
        given: "A rest client"
        def builder = new RestBuilder()

        when: "A request is sent to a controller with a namespace"
        RestResponse resp = builder.get("${baseUrl}api/book/testRender")

        then: "The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.json.api == "version 1.0 (Namespaced)"
        resp.json.title == "API - The Shining"
    }

    void "test respond(foo, view: ..) in controllers with namespaces works"() {
        given: "A rest client"
        def builder = new RestBuilder()

        when: "A request is sent to a controller with a namespace"
        RestResponse resp = builder.get("${baseUrl}api/book/testRespond")

        then: "The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.json.api == "version 1.0 (Namespaced)"
        resp.json.title == "API - The Shining"
    }

    void "test respond(foo, view: ..) in controllers with namespaces works, view outside of namespace"() {
        given: "A rest client"
        def builder = new RestBuilder()

        when: "A request is sent to a controller with a namespace"
        RestResponse resp = builder.get("${baseUrl}api/book/testRespondOutsideNamespace")

        then: "The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.json.api == "version 1.0 (Non-Namespaced)"
        resp.json.title == "API - The Shining"
    }

    @Issue("https://github.com/grails/grails-views/issues/186")
    void "test view rendering with a namespace from a map"() {
        given: "A rest client"
        def builder = new RestBuilder()

        when: "A request is sent to a controller with a namespace"
        RestResponse resp = builder.get("${baseUrl}api/book/message")

        then: "The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.json.message == "Controller says Hello API"
    }
}
