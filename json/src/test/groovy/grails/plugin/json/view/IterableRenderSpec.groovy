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
}
