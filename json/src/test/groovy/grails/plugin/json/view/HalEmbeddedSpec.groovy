package grails.plugin.json.view

import grails.gorm.annotation.Entity
import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

/**
 * Created by graemerocher on 20/05/16.
 */
class HalEmbeddedSpec extends Specification implements JsonViewTest {
    void setup() {
        mappingContext.addPersistentEntities(Team, Player)
    }
    void "test hal render method for one-to-many associations"() {

        when:"A GSON view that renders hal.render(..) is rendered"


        def player = new Player(id: 1L, name: "Cantona")
        player.id = 1L

        def captain = new Player(name: "Keane")
        captain.id = 2L
        def team = new Team( captain: captain, name: "Manchester United", players: [player])
        team.id = 1L
        def result = render('''
import grails.plugin.json.view.*
model {
    Team team
}
json hal.render(team)
''', [team: team])

        then:'the result is correct'
        result.jsonText == '{"_embedded":{"captain":{"_links":{"self":{"href":"http://localhost:8080/player/2","hreflang":"en","type":"application/hal+json"}},"name":"Keane"},"players":[{"_links":{"self":{"href":"http://localhost:8080/player/1","hreflang":"en","type":"application/hal+json"}},"name":"Cantona"}]},"_links":{"self":{"href":"http://localhost:8080/team/1","hreflang":"en","type":"application/hal+json"}},"id":1,"name":"Manchester United"}'
        result.json.'_embedded'
    }

    void "test hal embedded method for one-to-many associations"() {
        when:"A GSON view that renders hal.embedded(..) is rendered"


        def player = new Player(id: 1L, name: "Cantona")
        player.id = 1L

        def captain = new Player(name: "Keane")
        captain.id == 1L
        def team = new Team( captain: captain, name: "Manchester United", players: [player])
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
        result.jsonText == '{"_embedded":{"captain":{"_links":{"self":{"href":"http://localhost:8080/player","hreflang":"en","type":"application/hal+json"}},"name":"Keane"},"players":[{"_links":{"self":{"href":"http://localhost:8080/player/1","hreflang":"en","type":"application/hal+json"}},"name":"Cantona"}]},"name":"Manchester United"}'
        result.json.'_embedded'
    }

    void "test hal embedded method for many-to-one associations"() {
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

    void "test hal embedded with associations that have GORM embedded properties"() {
        given:"A domain class with embedded associations"
        mappingContext.addPersistentEntities(Person, Parent)
        Person p = new Person(name:"Robert")
        p.homeAddress = new Address(postCode: "12345")
        p.otherAddresses = [new Address(postCode: "6789"), new Address(postCode: "54321")]
        p.nickNames = ['Rob','Bob']
        def parent = new Parent(name: "Joe", person: p)

        when:"hal.render(..) is used"

        def result = render('''
import grails.plugin.json.view.*
model {
    Parent parent
}
json hal.render(parent)
''', [parent:parent])

        then:"The result is correct"
        result.jsonText == '{"_embedded":{"person":{"_links":{"self":{"href":"http://localhost:8080/person","hreflang":"en","type":"application/hal+json"}},"homeAddress":{"postCode":"12345"},"name":"Robert","nickNames":["Rob","Bob"],"otherAddresses":[{"postCode":"6789"},{"postCode":"54321"}]}},"_links":{"self":{"href":"http://localhost:8080/parent","hreflang":"en","type":"application/hal+json"}},"name":"Joe"}'


    }
}

@Entity
class Parent {
    String name
    Person person
}

