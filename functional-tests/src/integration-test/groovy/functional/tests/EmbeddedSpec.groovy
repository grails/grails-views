package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.web.http.HttpHeaders

@Integration
class EmbeddedSpec extends GebSpec {

    void "Test render can handle a domain with an embedded src/groovy class"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("${baseUrl}embedded")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'

        resp.text == '{"id":1,"customClass":{"name":"Bar"},"name":"Foo"}'
    }


}
