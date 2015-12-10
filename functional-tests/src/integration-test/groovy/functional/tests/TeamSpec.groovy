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
        resp.text == '{"id":1,"captain":{"id":1,"name":"Iniesta","sport":"football"},"name":"Barcelona","players":[{"id":1},{"id":2}],"sport":"football"}'
    }

    void "Test deep association template rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("$baseUrl/teams/deep/1")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"captain":{"id":1,"name":"Iniesta","sport":"football"},"name":"Barcelona","players":[{"id":1,"name":"Iniesta"},{"id":2,"name":"Messi"}],"sport":"football"}'
    }
}
