package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class ProjectSpec extends GebSpec {

    void "Test that circular references are correctly rendered for many to many relationship"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:"A POST is issued"

        def resp = builder.get("${baseUrl}project/")
        def json = resp.json

        then:"The correct response is returned"
        resp.status == 200
        json.id == 1
        json.name == "Grails Views"
        json.employees.find { it.id == 1 }.name == "James Kleeh"
        json.employees.find { it.id == 1 }.project == null
        json.employees.find { it.id == 2 }.name == "Iván López"
        json.employees.find { it.id == 2 }.project == null
    }
}
