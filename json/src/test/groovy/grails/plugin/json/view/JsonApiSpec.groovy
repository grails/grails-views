package grails.plugin.json.view

import grails.persistence.Entity
import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

class JsonApiSpec extends Specification implements JsonViewTest {
    void setup() {
        mappingContext.addPersistentEntities(Widget)
    }

    void 'test simple case'() {
        when:
            Widget theWidget = new Widget(name: 'One', width: 4, height: 7)

            def result = render('''
import grails.plugin.json.view.Widget
model {
    Widget widget
}

json jsonapi.render(widget)
''', [widget: theWidget])

        then:
            result.jsonText == '''{"data": {"type":"widget","id":"/domain/50/widgets/4","attributes":{"height":7,"name":"One","width":4}}}'''
    }
}

@Entity
class Widget {
    String name
    int width
    int height
}