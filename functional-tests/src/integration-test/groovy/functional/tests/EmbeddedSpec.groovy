package functional.tests

import grails.test.mixin.integration.Integration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import spock.lang.Issue

import static io.micronaut.http.HttpHeaders.CONTENT_TYPE

@Integration
class EmbeddedSpec extends HttpClientSpec {

    void "Test render can handle a domain with an embedded src/groovy class"() {
        when:
        HttpRequest request = HttpRequest.GET('/embedded')
        HttpResponse<String> rsp = client.toBlocking().exchange(request, String)

        then:"The response is correct"
        rsp.status() == HttpStatus.OK
        rsp.getHeaders().get(CONTENT_TYPE)  == 'application/json;charset=UTF-8'

        rsp.body() == '{"id":1,"customClass":{"name":"Bar"},"inSameFile":{"text":"FooBar"},"name":"Foo"}'
    }

    void "Test jsonapi render can handle a domain with an embedded src/groovy class"() {
        when:
        HttpRequest request = HttpRequest.GET('/embedded/jsonapi')
        HttpResponse<String> rsp = client.toBlocking().exchange(request, String)

        then:"The response is correct"
        rsp.status() == HttpStatus.OK
        rsp.getHeaders().get(CONTENT_TYPE)  == 'application/json;charset=UTF-8'

        rsp.body() == '{"data":{"type":"embedded","id":"2","attributes":{"customClass":{"name":"Bar2"},"inSameFile":{"text":"FooBar2"},"name":"Foo2"}},"links":{"self":"/embedded/show/2"}}'
    }

    @Issue("https://github.com/grails/grails-views/issues/171")
    void 'test render can handle a domain with an embedded and includes src/groovy class'() {
        when:
        HttpRequest request = HttpRequest.GET('/embedded/embeddedWithIncludes')
        HttpResponse<String> rsp = client.toBlocking().exchange(request, String)

        then: 'the response is correct'
        rsp.status() == HttpStatus.OK
        rsp.getHeaders().get(CONTENT_TYPE)  == 'application/json;charset=UTF-8'
        rsp.body() == '{"customClass":{"name":"Bar3"},"name":"Foo3"}'
    }

    @Issue("https://github.com/grails/grails-views/issues/171")
    void 'Test jsonapi render can handle a domain with an embedded and includes src/groovy class'() {
        when:
        HttpRequest request = HttpRequest.GET('/embedded/embeddedWithIncludesJsonapi')
        HttpResponse<String> rsp = client.toBlocking().exchange(request, String)

        then: 'the response is correct'
        rsp.status() == HttpStatus.OK
        rsp.getHeaders().get(CONTENT_TYPE)  == 'application/json;charset=UTF-8'
        rsp.body() == '{"data":{"type":"embedded","id":"4","attributes":{"customClass":{"name":"Bar4"},"name":"Foo4"}},"links":{"self":"/embedded/show/4"}}'
    }
}
