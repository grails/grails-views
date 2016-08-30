package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.web.http.HttpHeaders

@Integration
@Rollback
class CircularSpec extends GebSpec {

    void "test deep rendering of circular domain relationships"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:"A GET is issued"

        def resp = builder.get("$baseUrl/circular/show/1")

        then:"The REST resource is retrieved and the correct JSON is returned"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"circulars":[{"id":3,"circulars":[],"name":"topLevel-3","parent":{"id":1}},{"id":2,"circulars":[],"name":"topLevel-2","parent":{"id":1}}],"name":"topLevel"}'
    }
}
