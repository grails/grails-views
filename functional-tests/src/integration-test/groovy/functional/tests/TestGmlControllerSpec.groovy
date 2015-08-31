package functional.tests

import grails.test.mixin.integration.Integration
import grails.transaction.*
import groovy.util.slurpersupport.GPathResult
import spock.lang.*
import geb.spock.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Integration
@Rollback
class TestGmlControllerSpec extends GebSpec {

    void "Test GML response from action that returns a model"() {
        when:"When an action that renders a GML view is requested"
        def uri = "${baseUrl}/testGml/testView"
        GPathResult content = parseXml(uri)

        then:"The XML view is rendered"
        content.car.size() == 1
        content.car[0].@make.text() == "Audi"
    }

    protected GPathResult parseXml(String uri) {
        def content = new XmlSlurper().parseText(new URL(uri).text)
        content
    }
}
