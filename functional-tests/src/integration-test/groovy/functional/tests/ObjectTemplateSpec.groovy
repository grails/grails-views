package functional.tests

import grails.test.mixin.integration.Integration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

@Integration
class ObjectTemplateSpec extends HttpClientSpec {

    void "Test that if there is a global /object/_object template it is rendered if no template found"() {
        when:"A POST is issued"
        HttpRequest request = HttpRequest.GET('/place/test')
        HttpResponse<String> rsp = client.toBlocking().exchange(request, String)

        then:"The correct response is returned"
        rsp.status() == HttpStatus.OK
        rsp.body() == '{"location":{"type":"Point","coordinates":[10.0,10.0]},"name":"London"}'
    }
}