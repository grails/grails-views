package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.web.http.HttpHeaders
import org.springframework.beans.factory.annotation.Value

/**
 */
@Integration
class TeamSpec extends GebSpec {

    @Value('${local.server.port}')
    Integer port

    void "Test association template rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("${baseUrl}teams/1")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'

        // Note current behaviour is that the captain is not rendered twice
        resp.text == '{"id":1,"captain":{"id":1},"name":"Barcelona","players":[{"id":1},{"id":2}],"sport":"football"}'
    }

    void "Test deep association template rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("${baseUrl}teams/deep/1")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"captain":{"id":1,"name":"Iniesta","sport":"football"},"name":"Barcelona","players":[{"id":1,"name":"Iniesta","sport":"football"},{"id":2,"name":"Messi","sport":"football"}],"sport":"football"}'
    }

    void "Test HAL rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("${baseUrl}teams/hal/1")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/hal+json;charset=UTF-8'
        resp.text == '{"_embedded":{"captain":{"_links":{"self":{"href":"http://localhost:'+port+'/player/show/1","hreflang":"en_US","type":"application/hal+json"}},"name":"Iniesta","version":0},"players":[{"_links":{"self":{"href":"http://localhost:'+port+'/player/show/1","hreflang":"en_US","type":"application/hal+json"}},"name":"Iniesta","version":0},{"_links":{"self":{"href":"http://localhost:'+port+'/player/show/2","hreflang":"en_US","type":"application/hal+json"}},"name":"Messi","version":0}]},"_links":{"self":{"href":"http://localhost:'+port+'/teams/1","hreflang":"en_US","type":"application/hal+json"}},"id":1,"name":"Barcelona","sport":"football","another":{"foo":"bar"}}'

    }

    void "Test composite ID rendering"() {
        Composite.withNewSession {
            new Composite(name: "foo", team: Team.load(1), player: Player.load(2)).save(flush: true, failOnError: true)
        }
        def builder = new RestBuilder()

        when:
        def resp = builder.get("$baseUrl/team/composite")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"player":{"id":2,"name":"Messi","sport":"football"},"team":{"id":1,"captain":{"id":1},"name":"Barcelona","sport":"football"},"name":"foo"}'
    }
}
