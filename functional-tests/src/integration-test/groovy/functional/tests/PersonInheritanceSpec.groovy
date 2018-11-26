package functional.tests

import grails.test.mixin.integration.Integration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

@Integration
class PersonInheritanceSpec extends HttpClientSpec {

    void 'test template inheritance produces correct json'() {
        when:
        HttpRequest request = HttpRequest.GET('/person-inheritance')
        HttpResponse<String> rsp = client.toBlocking().exchange(request, String)

        then: 'the response is correct'
        rsp.status() == HttpStatus.OK
        rsp.body() == '{"dob":"01/01/1970","lastName":"Doe","firstName":"John"}'
    }
}
