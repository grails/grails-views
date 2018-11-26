package functional.tests

import grails.test.mixin.integration.Integration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

@Integration
class VehicleSpec extends HttpClientSpec {

    void "Test that domain subclasses render their properties"() {
        when:
        HttpRequest request = HttpRequest.GET('/vehicle/list')
        HttpResponse<String> resp = client.toBlocking().exchange(request, String)

        then:"The correct response is returned"
        resp.status == HttpStatus.OK
        resp.body() == '[{"id":1,"maxPassengers":30,"route":"around town"},{"id":2,"make":"Subaru","maxPassengers":4,"model":"WRX","year":2016}]'

    }

    void "Test that domain association subclasses render their properties"() {
        when:
        HttpRequest request = HttpRequest.GET('/vehicle/garage')
        HttpResponse<Map> resp = client.toBlocking().exchange(request, Map)
        def json = resp.body()

        then:"The correct response is returned"
        resp.status == HttpStatus.OK

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