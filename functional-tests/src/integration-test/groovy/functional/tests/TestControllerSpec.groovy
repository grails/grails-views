package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Issue

@Integration
@Rollback
class TestControllerSpec extends GebSpec {

    @Issue('https://github.com/grails/grails-core/issues/10582')
    void 'test responding after an action triggered by a HTTP 401 response is possible'() {
        given: 'a rest client'
        def builder = new RestBuilder()

        when:
        def resp = builder.get("${baseUrl}/test/triggerUnauthorized")

        then: 'the response is correct'
        resp.status == 401
        resp.text == '{"message":"Unauthorized GSON"}'
    }
}
