package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class VehicleSpec extends GebSpec {

    void "Test that domain subclasses render their properties"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:"A POST is issued"

        def resp = builder.get("${baseUrl}vehicle/list")

        then:"The correct response is returned"
        resp.status == 200
        resp.text == '[{"id":1,"maxPassengers":30,"route":"around town"},{"id":2,"make":"Subaru","maxPassengers":4,"model":"WRX","year":2016}]'

    }

    void "Test that domain association subclasses render their properties"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:"A POST is issued"

        def resp = builder.get("${baseUrl}vehicle/garage")
        def json = resp.json

        then:"The correct response is returned"
        resp.status == 200
        json.id == 1
        json.owner == "Jay Leno"
        json.vehicles.find { it.id == 1 }.maxPassengers == 30
        json.vehicles.find { it.id == 1 }.route == "around town"
        json.vehicles.find { it.id == 2 }.maxPassengers == 4
        json.vehicles.find { it.id == 2 }.make == "Subaru"
        json.vehicles.find { it.id == 2 }.model == "WRX"
        json.vehicles.find { it.id == 2 }.year == 2016
    }
}