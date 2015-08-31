package functional.tests

import grails.test.mixin.integration.Integration
import grails.transaction.*

import spock.lang.*
import geb.spock.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Integration
@Rollback
class TestControllerSpec extends GebSpec {

    void "Test the respond method returns a GSON view for JSON request"() {
        when:"When JSON is requested"
        def content = new URL("${baseUrl}/testGson/testRespond.json").text

        then:"The JSON view is rendered"
        content == '{"test":{"name":"Bob"}}'

        when:"When HTML is requested"
        go '/testGson/testRespond'

        then:"The GSP is rendered"
        $('h1').text() == "Test Bob HTML"
    }

    void "Test the respond method returns a GSON named after the domain view for JSON request"() {
        when:"When JSON is requested"
        def content = new URL("${baseUrl}/testGson/testRespondWithTemplateForDomain.json").text

        then:"The JSON view is rendered"
        content == '{"test":{"name":"Bob","age":60}}'
    }

    void "Test template rendering works"() {
        when:"A view that renders templates is rendered"
        def content = new URL("${baseUrl}/testGson/testTemplate.json").text

        then:"The result is correct"
        content == '{"test":{"name":"Bob","child":{"child":{"name":"Joe","age":10}},"children":[{"child":{"name":"Joe","age":10}}]}}'
    }

}
