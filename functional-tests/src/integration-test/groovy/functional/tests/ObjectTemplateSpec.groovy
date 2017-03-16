package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

/**
 * Created by graemerocher on 23/05/16.
 */
@Integration
@Rollback
class ObjectTemplateSpec extends GebSpec {

    void "Test that if there is a global /object/_object template it is rendered if no template found"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:"A POST is issued"

        def resp = builder.get("${baseUrl}place/test")

        then:"The correct response is returned"
        resp.status == 200
        resp.text == '{"location":{"type":"Point","coordinates":[10.0,10.0]},"name":"London"}'

    }
}