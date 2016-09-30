package grails.plugin.json.view

import grails.plugin.json.view.test.JsonViewTest
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class IterableRenderSpec extends Specification implements JsonViewTest {

    void "Test render a collection type"() {
        given:"A collection"
        def players = [new Player(name:"Cantona")]

        when:"A collection type is rendered"
        def renderResult = render('''
import groovy.transform.*
import grails.plugin.json.view.*

@Field Collection<Player> players

json g.render(players)
''', [players:players])

        then:"The result is an array"
        renderResult.jsonText == '[{"name":"Cantona"}]'

    }


    void "Test render a collection type with HAL"() {
        given:"A collection"
        def players = [new Player(name:"Cantona")]

        when:"A collection type is rendered"
        def renderResult = render('''
import groovy.transform.*
import grails.plugin.json.view.*

@Field Collection<Player> players

json hal.render(players)
''', [players:players])

        then:"The result is an array"
        renderResult.jsonText == '{"_links":{"self":{"href":"http://localhost:8080/player","hreflang":"en","type":"application/hal+json"}},"_embedded":[{"_links":{"self":{"href":"http://localhost:8080/player","hreflang":"en","type":"application/hal+json"}},"name":"Cantona"}]}'

    }

    void "Test render a single element collection type with JSON API"() {
        given: "A collection"
        mappingContext.addPersistentEntities(Player, Team)
        Player player = new Player(name: "Cantona")
        player.id = 1
        def players = [player]

        when: "A collection type is rendered"
        def renderResult = render('''
import groovy.transform.*
import grails.plugin.json.view.*

@Field Collection<Player> players

json jsonapi.render(players)
''', [players: players]) {
            uri = "/foo"
        }

        then: "The result is an array"
        renderResult.jsonText == '{"data":[{"type":"player","id":"1","attributes":{"name":"Cantona","version":null},"relationships":{"team":{"data":{"type":"team","id":null}}}}],"links":{"self":"/foo"}}'
    }

    void "Test render a collection type with JSON API"() {
        given: "A collection"
        mappingContext.addPersistentEntities(Player, Team)
        Player player = new Player(name: "Cantona")
        player.id = 1
        Player player2 = new Player(name: "Louis")
        player2.id = 2
        def players = [player, player2]

        when: "A collection type is rendered"
        def renderResult = render('''
import groovy.transform.*
import grails.plugin.json.view.*

@Field Collection<Player> players

json jsonapi.render(players)
''', [players: players]) {
            uri = "/foo"
        }

        then: "The result is an array"
        renderResult.jsonText == '{"data":[{"type":"player","id":"1","attributes":{"name":"Cantona","version":null},"relationships":{"team":{"data":{"type":"team","id":null}}}},{"type":"player","id":"2","attributes":{"name":"Louis","version":null},"relationships":{"team":{"data":{"type":"team","id":null}}}}],"links":{"self":"/foo"}}'
    }
}
