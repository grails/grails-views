package grails.plugin.json.view

import grails.persistence.Entity
import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

/**
 * Created by graemerocher on 22/04/16.
 */
class EnumRenderingSpec extends Specification implements JsonViewTest {

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
}

@Entity
class EnumTest {
    String name
    TestEnum bar
}

enum TestEnum { FOO, BAR}


