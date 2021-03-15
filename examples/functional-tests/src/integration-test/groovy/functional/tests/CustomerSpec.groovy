package functional.tests

import grails.testing.mixin.integration.Integration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

@Integration(applicationClass = Application)
class CustomerSpec extends HttpClientSpec {

    void "Test that circular references are correctly rendered for one to many relationship"() {
        when:
        HttpRequest request = HttpRequest.GET("/customer")
        HttpResponse<Map> resp = client.toBlocking().exchange(request, Map)
        Map json = resp.body()

        then:"The correct response is returned"
        resp.status == HttpStatus.OK
        json.id == 1
        json.name == "Nokia"
        json.sites.find { it.id == 1 }.name == "Salo"
        json.sites.find { it.id == 1 }.customer == null
        json.sites.find { it.id == 2 }.name == "Helsinki"
        json.sites.find { it.id == 2 }.customer == null
     }
}
