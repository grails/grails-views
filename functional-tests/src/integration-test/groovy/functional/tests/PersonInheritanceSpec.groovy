package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration

@Integration
class PersonInheritanceSpec extends GebSpec {

    void 'test template inheritance produces correct json'() {
        given: 'a rest client'
        def builder = new RestBuilder()

        when:
        def resp = builder.get("${baseUrl}person-inheritance")

        then: 'the response is correct'
        resp.status == 200

        resp.text == '{"dob":"01/01/1970","lastName":"Doe","firstName":"John"}'
    }
}
