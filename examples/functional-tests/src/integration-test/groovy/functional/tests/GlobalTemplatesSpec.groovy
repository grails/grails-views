package functional.tests

import grails.testing.mixin.integration.Integration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

/**
 * Created by graemerocher on 19/05/16.
 */
@Integration(applicationClass = Application)
class GlobalTemplatesSpec extends HttpClientSpec {

    void "Test errors view rendering"() {
        when:
        HttpRequest request = HttpRequest.GET('/place/show')
        HttpResponse<String> rsp = client.toBlocking().exchange(request, String)

        then:"The REST resource is created and the correct JSON is returned"
        rsp.status() == HttpStatus.OK
        rsp.body() == '{"location":{"type":"Point","coordinates":[10.0,10.0]},"name":"London"}'
    }
}
