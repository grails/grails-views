package grails.plugin.json.view

import grails.plugin.json.view.test.JsonRenderResult
import grails.plugin.json.view.test.JsonViewTest
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.datastore.mapping.core.Session
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class ExpandSpec extends Specification implements JsonViewTest {

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
        result.jsonText == '{"name":"Cantona","team":{"id":1}}'

        when:"The domain is rendered with expand parameters"
        result = render(templateText, [player:player]) {
            params expand:'team'
        }

        then:"The association is expanded"
        result.jsonText == '{"name":"Cantona","team":{"id":1,"name":"Manchester United"}}'
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
        result.jsonText == '{"player":{"name":"Cantona","team":{"id":1,"name":"Manchester United"}}}'
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
        result.jsonText == '{"_embedded":{"team":{"_links":{"self":{"href":"http://localhost:8080/team/1","hreflang":"en","type":"application/hal+json"}}}},"_links":{"self":{"href":"http://localhost:8080/player","hreflang":"en","type":"application/hal+json"}},"name":"Cantona"}'
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
        result.jsonText == '{"data":{"type":"player","id":"3","attributes":{"name":"Cantona"},"relationships":{"team":{"links":{"self":"/team/9"},"data":{"type":"team","id":"9"}}}},"links":{"self":"/player/3"},"included":[{"type":"team","id":"9","attributes":{"name":"Manchester United","titles":null},"relationships":{"captain":{"data":null},"players":{"data":[]}},"links":{"self":"/team/9"}}]}'
    }
}
