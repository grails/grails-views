package grails.plugin.json.view

import grails.plugin.json.view.test.JsonRenderResult
import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

/**
 * Created by graemerocher on 15/04/2016.
 */
class JsonViewTestSpec extends Specification implements JsonViewTest {

    void "Test render a raw GSON view"() {
        when:"A gson view is rendered"
        def result = render'''
model {
    String person
}
json.person {
    name person
}
''', [person:"bob"]

        then:"The json is correct"
        result.json.person.name == 'bob'
    }

    void "Test render a GSON view"() {
        when:"A gson view is rendered"
        def result = render(view: "/foo") {
            method "POST"
        }

        then:"The json is correct"
        result.json.person.name == 'bob'
    }

    void "Test render a GSON template"() {
        when:"A gson view is rendered"
        def result = render(template: "/child", model:[age:10])

        then:"The json is correct"
        result.json.name == 'Fred'
        result.json.age == 10
    }

    void "Test render a GSON view that generates a link"() {
        when:"A gson view is rendered"
        def result = render(view: "/linkingView")

        then:"The json is correct"
        result.json.person.name == 'bob'
        result.json.person.homepage == '/person'
    }

    void "Test render a GSON view that sets headers"() {
        when:"A gson view is rendered"
        def result = render(view: "/pageConfigure")

        then:"The json is correct"
        result.json.person.name == 'Bob'
        result.headers['foo'] == 'bar'
        result.contentType == 'application/hal+json'
    }
}
