package grails.plugin.json.view

import grails.plugin.json.view.test.JsonRenderResult
import grails.plugin.json.view.test.JsonViewTest
import groovy.json.JsonSlurper
import org.grails.datastore.mapping.core.Session
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class ExpandSpec extends Specification implements JsonViewTest, GrailsUnitTest {

    void setup() {
        mappingContext.addPersistentEntities(Team, Player)
    }
    void "Test expand parameter allows expansion of child associations"() {

        given:"A entity with a proxy association"
        def mockSession = Mock(Session)
        mockSession.getMappingContext() >> mappingContext
        mockSession.retrieve(Team, 1L) >> new Team(name: "Manchester United")
        def teamProxy = mappingContext.proxyFactory.createProxy(mockSession, Team, 1L)

        Player player = new Player(name: "Cantona", team: teamProxy)

        def templateText = '''
import grails.plugin.json.view.*

@Field Player player

json g.render(player)
'''
        when:"The domain is rendered"
        def result = render(templateText, [player:player])

        then:"The result doesn't include the proxied association"
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['name'] == 'Cantona'
        m['team']['id'] == 1
        !m['team'].containsKey('name')

        when:"The domain is rendered with expand parameters"
        result = render(templateText, [player:player]) {
            params expand:'team'
        }
        m = new JsonSlurper().parseText(result.jsonText)

        then:"The association is expanded"
        m['name'] == 'Cantona'
        m['team']['id'] == 1
        m['team']['name'] == "Manchester United"
    }

    void "Test expand parameter on nested property"() {
        def mockSession = Mock(Session)
        mockSession.getMappingContext() >> mappingContext
        mockSession.retrieve(Team, 1L) >> new Team(name: "Manchester United")
        def teamProxy = mappingContext.proxyFactory.createProxy(mockSession, Team, 1L)

        Player player = new Player(name: "Cantona", team: teamProxy)
        def templateText = '''
import grails.plugin.json.view.*

@Field Map map

json g.render(map)
'''

        when:"The domain is rendered with expand parameters"
        def result = render(templateText, [map: [player:player]]) {
            params expand:'player.team'
        }

        then:"The association is expanded"
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['player']['name'] == 'Cantona'
        m['player']['team']['id'] == 1
        m['player']['team']['name'] == "Manchester United"
    }

    void "Test expand parameter allows expansion of child associations with HAL"() {

        given:"A entity with a proxy association"
        def mockSession = Mock(Session)
        mockSession.getMappingContext() >> mappingContext
        mockSession.retrieve(Team, 1L) >> new Team(name: "Manchester United")
        def teamProxy = mappingContext.proxyFactory.createProxy(mockSession, Team, 1L)

        Player player = new Player(name: "Cantona", team: teamProxy)

        def templateText = '''
import grails.plugin.json.view.*
model {
    Player player
}
json hal.render(player)
'''
        when:"The domain is rendered"
        def result = render(templateText, [player:player])

        then:"The result doesn't include the proxied association"
        result.jsonText == '{"_links":{"self":{"href":"http://localhost:8080/player","hreflang":"en","type":"application/hal+json"}},"name":"Cantona"}'

        when:"The domain is rendered with expand parameters"
        result = render(templateText, [player:player]) {
            params expand:'team'
        }

        then:"The association is expanded"
        result.jsonText == '{"_embedded":{"team":{"_links":{"self":{"href":"http://localhost:8080/team/1","hreflang":"en","type":"application/hal+json"}},"name":"Manchester United"}},"_links":{"self":{"href":"http://localhost:8080/player","hreflang":"en","type":"application/hal+json"}},"name":"Cantona"}'
    }

    void 'Test expand parameter allows expansion of child associations with JSON API'() {
        given:
        def mockSession = Mock(Session)
        mockSession.getMappingContext() >> mappingContext
        mockSession.retrieve(Team, 9L) >> new Team(name: "Manchester United")
        def teamProxy = mappingContext.proxyFactory.createProxy(mockSession, Team, 9L)
        Player player = new Player(name: "Cantona", team: teamProxy)
        player.id = 3


        when:
        JsonRenderResult result = render('''
import grails.plugin.json.view.*
model {
    Player player
}

json jsonapi.render(player, [expand: 'team'])
''', [player: player])

        then: 'The JSON relationships are in place'
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['data']['type'] == 'player'
        m['data']['id'] == '3'
        m['data']['attributes']['name'] == 'Cantona'
        m['data']['relationships']['team']['links']['self'] == "/team/9"
        m['data']['relationships']['team']['data']['type'] == "team"
        m['data']['relationships']['team']['data']['id'] == "9"
        m['links']['self'] == "/player/3"
        m['included'][0]['type'] == "team"
        m['included'][0]['id'] == "9"
        m['included'][0]['attributes']['name'] == "Manchester United"
        m['included'][0]['attributes']['titles'] == null
        m['included'][0]['relationships']['captain']['data'] == null
        m['included'][0]['relationships']['players']['data'] == []
        m['included'][0]['links']['self'] == "/team/9"
    }
}
