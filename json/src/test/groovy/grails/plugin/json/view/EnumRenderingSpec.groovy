package grails.plugin.json.view

import grails.persistence.Entity
import grails.plugin.json.view.test.JsonViewTest
import groovy.json.JsonSlurper
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class EnumRenderingSpec extends Specification implements JsonViewTest, GrailsUnitTest {

    void "Test the render method when a domain instance defines an enum"() {
        when:"rendering an object that defines an enum"
        mappingContext.addPersistentEntity(EnumTest)
        def result = render('''
model {
    Object object
}
json g.render(object)
''', [object: new EnumTest(name:"Fred", bar: TestEnum.BAR)])
        then:"the json is rendered correctly"
        result.json.bar == "BAR"
        result.json.name == "Fred"
    }

    void "Test the render method when a PGOO instance defines an enum"() {
        when:"rendering an object that defines an enum"
        def result = render('''
model {
    Object object
}
json g.render(object)
''', [object: new EnumTest(name:"Fred", bar: TestEnum.BAR)])
        then:"the json is rendered correctly"
        result.json.bar == "BAR"
        result.json.name == "Fred"
    }

    void "Test the jsonapi render method when a domain instance defines an enum"() {
        when:"rendering an object that defines an enum"
        mappingContext.addPersistentEntity(EnumTest)
        EnumTest enumTest = new EnumTest(name:"Fred", bar: TestEnum.BAR)
        enumTest.id = 1
        def result = render('''
model {
    Object object
}
json jsonapi.render(object)
''', [object: enumTest])

        then:"the json is rendered correctly"
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['data']['type'] == 'enumTest'
        m['data']['id'] == '1'
        m['data']['attributes']['bar'] == 'BAR'
        m['data']['attributes']['name'] == 'Fred'
        m['links']['self'] == "/enumTest/1"
    }
}

@Entity
class EnumTest {
    String name
    TestEnum bar
}

enum TestEnum { FOO, BAR}


