package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.web.http.HttpHeaders
import spock.lang.Specification

/**
 */
@Integration
class TeamSpec extends GebSpec{
    void "Test association template rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("$baseUrl/teams/1")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"captain":{"id":1,"name":"Iniesta","sport":"football"},"name":"Barcelona","players":[{"id":1,"name":"Iniesta","sport":"football"},{"id":2,"name":"Messi","sport":"football"}],"sport":"football"}'
    }

    void "Test deep association template rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("$baseUrl/teams/deep/1")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"captain":{"id":1,"name":"Iniesta","sport":"football"},"name":"Barcelona","players":[{"id":1,"name":"Iniesta","sport":"football"},{"id":2,"name":"Messi","sport":"football"}],"sport":"football"}'
    }

    void "Test HAL rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("$baseUrl/teams/hal/1")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/hal+json;charset=UTF-8'
        resp.text == '{"_embedded":{"captain":{"_links":{"self":{"href":"http://localhost:8080/player/show/1","hreflang":"en_US","type":"application/hal+json"}},"name":"Iniesta","version":0},"players":[{"_links":{"self":{"href":"http://localhost:8080/player/show/1","hreflang":"en_US","type":"application/hal+json"}},"_links":{"self":{"href":"http://localhost:8080/player/show/1","hreflang":"en_US","type":"application/hal+json"}},"name":"Iniesta","version":0},{"_links":{"self":{"href":"http://localhost:8080/player/show/2","hreflang":"en_US","type":"application/hal+json"}},"_links":{"self":{"href":"http://localhost:8080/player/show/2","hreflang":"en_US","type":"application/hal+json"}},"name":"Messi","version":0}]},"_links":{"self":{"href":"http://localhost:8080/teams/1","hreflang":"en_US","type":"application/hal+json"}},"id":1,"name":"Barcelona","sport":"football","another":{"foo":"bar"}}'

    }
}
