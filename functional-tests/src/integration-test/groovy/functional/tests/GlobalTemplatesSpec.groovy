package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.web.http.HttpHeaders

/**
 * Created by graemerocher on 19/05/16.
 */
@Integration
@Rollback
class GlobalTemplatesSpec extends GebSpec {

    def setup() {
    }

    def cleanup() {
    }

    void "Test errors view rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:"A POST is issued"

        def resp = builder.get("$baseUrl/place/show")

        then:"The REST resource is created and the correct JSON is returned"
        resp.status == 200
        resp.text == '{"location":{"type":"Point","coordinates":[10.0,10.0]},"name":"London"}'
    }
}
