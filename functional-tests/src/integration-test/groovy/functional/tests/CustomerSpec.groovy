package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class CustomerSpec extends GebSpec {

    void "Test that circular references are correctly rendered for one to many relationship"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:"A POST is issued"

        def resp = builder.get("${baseUrl}customer/")
        def json = resp.json

        then:"The correct response is returned"
        resp.status == 200
        json.id == 1
        json.name == "Nokia"
        json.sites.find { it.id == 1 }.name == "Salo"
        json.sites.find { it.id == 1 }.customer == null
        json.sites.find { it.id == 2 }.name == "Helsinki"
        json.sites.find { it.id == 2 }.customer == null
     }
}
