package functional.tests.api

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.web.http.HttpHeaders

@Integration
@Rollback
class NamespacedBookSpec extends GebSpec {

    void "test view rendering with a namespace"() {
        given: "A rest client"
            def builder = new RestBuilder()

        when: "A request is sent to a controller with a namespace"
            RestResponse resp = builder.get("$baseUrl/api/book")

        then: "The response is correct"
            resp.status == 200
            resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
            resp.json.api == "version 1.0 (Namespaced)"
            resp.json.title == "API - The Shining"
    }
}
