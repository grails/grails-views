package functional.tests

import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.*
import grails.web.http.HttpHeaders
import spock.lang.*
import geb.spock.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Integration
@Rollback
class BookSpec extends GebSpec {

    def setup() {
    }

    def cleanup() {
    }

    void "test REST view rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("$baseUrl/books")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '[]'

        when:"A POST is issued"

        resp = builder.post("$baseUrl/books") {
            json {
                title = "The Stand"
            }
        }

        then:"The REST resource is created and the correct JSON is returned"
        resp.status == 201
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"title":"The Stand","vendor":"MyCompany"}'

        when:"A GET request is issued"
        resp = builder.get("$baseUrl/books/${resp.json.id}")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"title":"The Stand","vendor":"MyCompany"}'

        when:"A PUT is issued"
        resp = builder.put("$baseUrl/books/${resp.json.id}") {
            json {
                title = "The Changeling"
            }
        }

        then:"The resource is updated"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"title":"The Changeling","vendor":"MyCompany"}'

        when:"A GET is issued for all books"
        resp = builder.get("$baseUrl/books")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '[{"id":1,"title":"The Changeling","vendor":"MyCompany"}]'

    }
}
