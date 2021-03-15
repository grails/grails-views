package functional.tests

import grails.testing.mixin.integration.Integration
import grails.web.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

@Integration(applicationClass = Application)
class InheritanceSpec extends HttpClientSpec {

    void "Test template is found for proxy instance that is initialized"() {
        when:
        HttpRequest request = HttpRequest.GET('/inheritance/multiNested')
        HttpResponse<String> resp = client.toBlocking().exchange(request, String)


        then:"The response is correct"
        resp.status == HttpStatus.OK
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).isPresent()
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).get() == 'application/json;charset=UTF-8'

        // Note current behaviour is that the captain is not rendered twice
        resp.body() == '{"levelTwo":true,"levelOne":true,"entry":true}'
    }
}