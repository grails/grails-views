package grails.plugin.json.view

import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

/**
 * Created by graemerocher on 14/10/16.
 */
class MapRenderSpec extends Specification implements JsonViewTest {

    void "Test render a map type"() {

        when:"An exception is rendered"
        def templateText = '''
model {
    Map map
}

json g.render(map)
'''
        def renderResult = render(templateText, [map:[foo:'bar']])

        then:"The exception is rendered"
        renderResult.json.foo == 'bar'

        when:"An entity is used in a map"
        mappingContext.addPersistentEntity(Player)
        renderResult = render(templateText, [map:[player1:new Player(name: "Cantona"), player2: new Player(name: "Giggs")]])

        then:"The result is correct"
        renderResult.jsonText == '{"player1":{"name":"Cantona"},"player2":{"name":"Giggs"}}'

    }

}