package functional.tests

import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.*
import grails.web.http.HttpHeaders
import org.springframework.beans.factory.annotation.Value
import spock.lang.*
import geb.spock.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Integration
@Rollback
class BookSpec extends GebSpec {

    @Value('${local.server.port}')
    Integer port

    def setup() {
    }

    def cleanup() {
    }

    void "Test errors view rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:"A POST is issued"

        def resp = builder.post("${baseUrl}books") {
            json {
                title = ""
            }
        }

        then:"The REST resource is created and the correct JSON is returned"
        resp.status == 422
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/vnd.error;charset=UTF-8'
        resp.text == '{"message":"Property [title] of class [class functional.tests.Book] cannot be null","path":"/book/index","_links":{"self":{"href":"http://localhost:'+port+'/book/index"}}}'
    }

    void "test REST view rendering"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("${baseUrl}books")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '[]'

        when:"A POST is issued"

        resp = builder.post("${baseUrl}books") {
            json {
                title = "The Stand"
            }
        }

        then:"The REST resource is created and the correct JSON is returned"
        resp.status == 201
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"timeZone":"America/New_York","title":"The Stand","vendor":"MyCompany"}'

        when:"A GET request is issued"
        resp = builder.get("${baseUrl}books/${resp.json.id}")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"timeZone":"America/New_York","title":"The Stand","vendor":"MyCompany"}'

        when:"A PUT is issued"
        resp = builder.put("${baseUrl}books/${resp.json.id}") {
            json {
                title = "The Changeling"
            }
        }

        then:"The resource is updated"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"id":1,"timeZone":"America/New_York","title":"The Changeling","vendor":"MyCompany"}'

        when:"A GET is issued for all books"
        resp = builder.get("${baseUrl}books")

        then:"The response is correct"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '[{"id":1,"timeZone":"America/New_York","title":"The Changeling","vendor":"MyCompany"}]'

        when:"A GET is issued for all books with excludes"
        resp = builder.get("${baseUrl}books/listExcludes?testParam=3")

        then:"Access to config and params works"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '[{"id":1,"timeZone":"America/New_York","title":"The Changeling","vendor":"ConfigVendor","fromParams":3}]'

        when:"A GET is issued for all books with excludes"
        resp = builder.get("${baseUrl}books/listExcludesRespond?testParam=4")

        then:"view rendering works with a map with respond"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '[{"id":1,"timeZone":"America/New_York","vendor":"ConfigVendor","fromParams":4}]'
    }

    void "View parameter passed to the render method can be used for non-standard view locations"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:"A GET is issued to a request with a template at a non-standard location"
        def resp = builder.get("${baseUrl}books/non-standard-template")

        then:"The template was rendered successfully. The custom converter was also used"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '{"bookTitle":"template found","custom":"Sally"}'
    }

    void "Object type of list is used for model variable when rendering templates"() {
        given:"A rest client"
        def builder = new RestBuilder()

        when:
        def resp = builder.get("${baseUrl}books/listCallsTmpl")

        then:"The template was rendered successfully"
        resp.status == 200
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/json;charset=UTF-8'
        resp.text == '[{"title":"The Changeling"}]'
    }
}
