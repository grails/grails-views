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
class TestGsonControllerSpec extends GebSpec {

    void "Test that responding with a map is possible"() {
        when:"When JSON is requested"
        def content = new URL("${baseUrl}/testGson/testRespondWithMap").text

        then:"The JSON view is rendered"
        content == '{"message":"two"}'

    }

    void "Test that responding with a map is possible with object template"() {
        when:"When JSON is requested"
        def content = new URL("${baseUrl}/testGson/testRespondWithMapObjectTemplate.json").text

        then:"The JSON view is rendered"
        content == '{"one":"two"}'

    }
    void "Test that it is possible to use the template engine directly"() {
        when:"When JSON is requested"
        def content = new URL("${baseUrl}/testGson/testTemplateEngine").text

        then:"The JSON view is rendered"
        content == '{"timeZone":"America/New_York","title":"The Stand","vendor":"MyCompany"}'
    }

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

    void "Test views from plugins are rendered"() {
        when:"A view that renders templates is rendered"
        def content = new URL("${baseUrl}/testGson/testGsonFromPlugin").text

        then:"The result is correct"
        content == '{"message":"Hello from Plugin"}'
    }

    void "Test view that inherits from plugins are rendered"() {
        when:
        def content = new URL("${baseUrl}/testGson/testInheritsFromPlugin").text

        then:
        content == '{"message":"Hello from Plugin Template","foo":"bar"}'
    }


    void "Test augmenting model"() {
        when:"When JSON is requested"
        def content = new URL("${baseUrl}/testGson/testAugmentModel.json").text

        then:"The JSON view is rendered"
        content == '{"test":{"name":"John","age":20}}'

        when:"When HTML is requested"
        go '/testGson/testAugmentModel'

        then:"The GSP is rendered"
        $('h1').text() == "Test John (20) HTML"
    }
}
