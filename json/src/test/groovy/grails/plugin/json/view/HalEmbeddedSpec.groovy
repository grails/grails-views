package grails.plugin.json.view

import grails.gorm.annotation.Entity
import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

/**
 * Created by graemerocher on 20/05/16.
 */
class HalEmbeddedSpec extends Specification implements JsonViewTest {

    void "test hal embedded method for one-to-many associations"() {
        mappingContext.addPersistentEntities(Team, Player)
        when:"A GSON view that renders hal.embedded(..) is rendered"


        def player = new Player(id: 1L, name: "Cantona")
        player.id = 1L
        def team = new Team( name: "Manchester United", players: [player])
        team.id = 1L
        def result = render('''
import grails.plugin.json.view.*
model {
    Team team
}
json {
    hal.embedded(team)
    name team.name
}
''', [team: team])

        then:'the result is correct'
        result.jsonText == '{"_embedded":{"players":[{"_links":{"self":{"href":"http://localhost:8080/player/1","hreflang":"en","type":"application/hal+json"}},"name":"Cantona"}]},"name":"Manchester United"}'
        result.json.'_embedded'
    }

    void "test hal embedded method for many-to-one associations"() {
        mappingContext.addPersistentEntities(Team, Player)
        when:"A GSON view that renders hal.embedded(..) is rendered"



        def team = new Team(name: "Manchester United")
        def player = new Player(id: 1L, name: "Cantona", team: team)
        team.players = [player]
        player.id = 1L
        team.id = 1L
        def result = render('''
import grails.plugin.json.view.*
model {
    Player player
}
json {
    hal.embedded(player)
    name player.name
}
''', [player: player])

        then:'the result is correct'
        result.jsonText == '{"_embedded":{"team":{"_links":{"self":{"href":"http://localhost:8080/team/1","hreflang":"en","type":"application/hal+json"}},"name":"Manchester United"}},"name":"Cantona"}'
        result.json.'_embedded'.team.name == "Manchester United"
    }
}

