package grails.plugin.json.view

import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

/**
 * Created by graemerocher on 22/04/16.
 */
class PogoSetRenderingSpec extends Specification implements JsonViewTest {

    void "Test render POGO that defines a set"() {
        when:"A pogo that defines a set is rendered"
        def result = render('''
model {
    Object object
}
json g.render(object)
''', [object: new SetClass(name: "Bob", stuff: ['one', 'two'] as Set)])

        then:"The JSON is correct"
        result.json.name == "Bob"
        result.json.stuff.contains('one')
        result.json.stuff.contains('two')

    }
}

class SetClass {
    String name
    Set stuff = []
}
