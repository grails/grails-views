package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.web.http.HttpHeaders

@Integration
class InheritanceSpec extends GebSpec {

    void "Test template is found for proxy instance that is initialized"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("${baseUrl}inheritance/multiNested")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'

        // Note current behaviour is that the captain is not rendered twice
        resp.text == '{"levelTwo":true,"levelOne":true,"entry":true}'
    }
}