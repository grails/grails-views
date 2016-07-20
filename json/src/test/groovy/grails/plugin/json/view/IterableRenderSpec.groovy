package grails.plugin.json.view

import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

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
}
