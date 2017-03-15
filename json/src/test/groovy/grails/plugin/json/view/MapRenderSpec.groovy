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

    void "Test render a map type with excludes"() {
        def templateText = '''
model {
    Map map
}

json g.render(map, [excludes: ['player1','player2.name']])
'''

        when:"An entity is used in a map"
        mappingContext.addPersistentEntity(PlayerWithAge)
        def renderResult = render(templateText, [map:[player1:new PlayerWithAge(name: "Cantona", age: 22), player2: new PlayerWithAge(name: "Giggs", age: 33)]])

        then:"The result is correct"
        renderResult.jsonText == '{"player2":{"age":33}}'

    }

    void "Test render a map type with excludes on a collection"() {
        def templateText = '''
model {
    Map map
}

json g.render(map, [excludes: ['players.name']])
'''

        when:"An entity is used in a map"
        mappingContext.addPersistentEntity(PlayerWithAge)
        def renderResult = render(templateText, [map:[players:[new PlayerWithAge(name: "Cantona", age: 22), new PlayerWithAge(name: "Giggs", age: 33)]]])

        then:"The result is correct"
        renderResult.jsonText == '{"players":[{"age":22},{"age":33}]}'
    }

    void "Test render a map type with a simple array"() {

        when:"A map is rendered"
        def templateText = '''
model {
    Map map
}

json g.render(map)
'''
        def renderResult = render(templateText, [map:[foo:'bar', bar: ['A','B']]])

        then:"The result is correct"
        renderResult.jsonText == '{"foo":"bar","bar":["A","B"]}'

    }

    void "Test render a list of maps"() {
        when:
        def templateText = '''
model {
    List list
}

json g.render(list)
'''
        def renderResult = render(templateText, [list:[[foo:'bar', bar: ['A','B']], [x:'y']]])

        then:"The result is correct"
        renderResult.jsonText == '[{"foo":"bar","bar":["A","B"]},{"x":"y"}]'
    }



}

