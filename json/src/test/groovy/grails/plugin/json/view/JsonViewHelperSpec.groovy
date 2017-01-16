package grails.plugin.json.view

import grails.core.DefaultGrailsApplication
import grails.core.GrailsDomainClass
import grails.persistence.Entity
import grails.plugin.json.view.api.JsonView
import grails.plugin.json.view.api.internal.DefaultGrailsJsonViewHelper
import grails.plugin.json.view.test.JsonViewTest
import grails.views.GrailsViewTemplate
import grails.views.api.internal.EmptyParameters
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.core.support.GrailsDomainConfigurationUtil
import org.grails.datastore.gorm.config.GrailsDomainClassMappingContext
import org.grails.datastore.gorm.config.GrailsDomainClassPersistentEntity
import org.grails.datastore.mapping.model.MappingContext
import spock.lang.Specification

/*
 * Copyright 2014 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author graemerocher
 */
class JsonViewHelperSpec extends Specification implements JsonViewTest {
    void "test render toMany association"() {
        given:"A view helper"
        DefaultGrailsJsonViewHelper viewHelper = mockViewHelper(Team, Player)
        def player1 = new Player(name: "Iniesta")
        def player2 = new Player(name: "Messi")
        def team = new Team(name:"Barcelona", players: [player1, player2])

        when:"We render an object without deep argument and no child id"

        def result = viewHelper.render(team)

        then:"The result is correct"
        result.toString() == '{"name":"Barcelona","players":[{"name":"Iniesta"},{"name":"Messi"}]}'

        when:"We render an object without deep argument and child ids"

        player1.id = 1L
        player2.id = 2L
        result = viewHelper.render(team)

        then:"The result is correct"
        result.toString() == '{"name":"Barcelona","players":[{"id":1},{"id":2}]}'

        when:"We render an object with deep argument and child ids"

        player1.id = 1L
        player2.id = 2L
        result = viewHelper.render(team, [deep: true])

        then:"The result is correct"
        result.toString() == '{"name":"Barcelona","players":[{"id":1,"name":"Iniesta"},{"id":2,"name":"Messi"}]}'

    }
    void "test render toOne association"() {
        given:"A view helper"
        DefaultGrailsJsonViewHelper viewHelper = mockViewHelper(Team, Player)

        when:"We render an object without deep argument and no child id"

        def player = new Player(name: "Iniesta")
        def team = new Team(name:"Barcelona",titles: ['La Liga'], captain: player)
        def result = viewHelper.render(team)

        then:"The result is correct"
        result.toString() == '{"name":"Barcelona","titles":["La Liga"]}'

        when:"We render an object with deep argument and no child id"

        result = viewHelper.render(team, [deep:true])

        then:"The result is correct"
        result.toString() == '{"captain":{"name":"Iniesta"},"name":"Barcelona","titles":["La Liga"]}'

        when:"We render an object without deep argument and a child id"

        player.id = 1L
        result = viewHelper.render(team)

        then:"The result is correct"
        result.toString() == '{"captain":{"id":1},"name":"Barcelona","titles":["La Liga"]}'

        when:"We render an object with deep argument and a child id"

        player.id = 1L
        result = viewHelper.render(team, [deep:true])

        then:"The result is correct"
        result.toString() == '{"captain":{"id":1,"name":"Iniesta"},"name":"Barcelona","titles":["La Liga"]}'

        when:"We render an object with deep argument and a child id and excludes"

        player.id = 1L
        result = viewHelper.render(team, [deep:true, excludes: ['captain.name']])

        then:"The result is correct"
        result.toString() == '{"captain":{"id":1},"name":"Barcelona","titles":["La Liga"]}'

        when:"We render an object with deep argument and a child id and includes"

        player.id = 1L
        result = viewHelper.render(team, [deep:true, includes: ['captain','captain.name']])

        then:"The result is correct"
        result.toString() == '{"captain":{"name":"Iniesta"}}'
    }

    void "Test render object method with customizer"() {
        given:"A view helper"
        DefaultGrailsJsonViewHelper viewHelper = mockViewHelper(Test)

        when:"We render an object"

        def test = new Test(title: "The Stand", author: new TestAuthor(name: "Stephen King"))
        test.id = 1L
        def result = viewHelper.render(test) {
            pages 1000
        }
        then:"The result is correct"
        result.toString() == '{"id":1,"author":{"name":"Stephen King"},"title":"The Stand","pages":1000}'
    }

    void "Test render object method with customizer when not configured as a domain"() {
        given:"A view helper"
        DefaultGrailsJsonViewHelper viewHelper = mockViewHelper()

        when:"We render an object"

        def test = new Test(title: "The Stand", author: new TestAuthor(name: "Stephen King"))
        test.id = 1L
        def result = viewHelper.render(test) {
            pages 1000
        }
        then:"The result is correct"
        result.toString() == '{"author":{"name":"Stephen King"},"id":1,"title":"The Stand","pages":1000}'
    }

    void "Test render object method"() {
        given:"A view helper"
        def viewHelper = mockViewHelper(Test)

        when:"We render an object"

        def test = new Test(title: "The Stand", author: new TestAuthor(name: "Stephen King"))
        test.id = 1L
        def result = viewHelper.render(test)
        then:"The result is correct"
        result.toString() == '{"id":1,"author":{"name":"Stephen King"},"title":"The Stand"}'

        when:"We render an object"
        result = viewHelper.render(new Test(title:"The Stand", author:new TestAuthor(name:"Stephen King")), [excludes:['author']])
        then:"The result is correct"
        result.toString() == '{"title":"The Stand"}'
    }

    void "Test render object method with plain object"() {
        given:"A view helper"
        def viewHelper = mockViewHelper(Test)

        when:"We render an object"
        def result = viewHelper.render(new Test2(title:"The Stand", author:"Stephen King"))
        then:"The result is correct"
        result.toString() == '{"author":"Stephen King","title":"The Stand"}'

        when:"We render an object"
        result = viewHelper.render(new Test2(title:"The Stand", author:"Stephen King"), [excludes:['author']])
        then:"The result is correct"
        result.toString() == '{"title":"The Stand"}'
    }

    void "test expand"() {
        given: "An view helper"
        def viewHelper = mockViewHelper(Player, Team)
        def player1 = new Player(name: "Iniesta")
        player1.id = 1
        def player2 = new Player(name: "Messi")
        player2.id = 2
        def team = new Team(name: "Barcelona", players: [player1, player2])
        team.id = 1
        player1.setTeam(team)
        player2.setTeam(team)

        when: "We render an object with single expand"

        def renderResult = viewHelper.render(team, [expand: 'players'])

        then: "The result is correct"
        renderResult.toString() == '{"id":1,"name":"Barcelona","players":[{"id":1,"name":"Iniesta","team":{"id":1}},{"id":2,"name":"Messi","team":{"id":1}}]}'

        when: "We render an object with nested expand"

        renderResult = viewHelper.render(team, [expand: 'players.team'])

        then: "The result is correct"
        renderResult.toString() == '{"id":1,"name":"Barcelona","players":[{"id":1,"name":"Iniesta","team":{"id":1,"name":"Barcelona","players":[{"id":1},{"id":2}]}},{"id":2,"name":"Messi","team":{"id":1,"name":"Barcelona","players":[{"id":1},{"id":2}]}}]}'
    }

    void "Test render collection as null produces empty list"() {
        given:"A view helper"
        def viewHelper = mockViewHelper()

        when:
        def result = viewHelper.render(collection: null, template: 'child', var: 'age')

        then:"The result is correct"
        result.toString() == '[]'

        when:
        result = viewHelper.render(collection: [], template: 'child', var: 'age')

        then:"The result is correct"
        result.toString() == '[]'
    }

    protected DefaultGrailsJsonViewHelper mockViewHelper(Class...classes) {
        def jsonView = Mock(JsonView)
        jsonView.getParams() >> new EmptyParameters()
        jsonView.getExpandRootArguments() >> [:]
        def mappingContext = Mock(MappingContext)

        def app = new DefaultGrailsApplication(classes)
        app.initialise()
        def domainClasses = app.getArtefacts(DomainClassArtefactHandler.TYPE)
        def domainMap = domainClasses.collectEntries { GrailsDomainClass dc ->
            [(dc.fullName): dc]
        }
        GrailsDomainConfigurationUtil.configureDomainClassRelationships(domainClasses,domainMap)


        def mockMappingContxt = new GrailsDomainClassMappingContext(app)
        for(dc in domainClasses) {
            def entity = new GrailsDomainClassPersistentEntity(dc, mockMappingContxt)
            entity.initialize()
            mappingContext.getPersistentEntity(dc.fullName) >> entity
        }
        jsonView.getMappingContext() >> mappingContext

        def viewHelper = new DefaultGrailsJsonViewHelper(jsonView)

        def binding = new Binding()
        jsonView.getBinding() >> binding

        jsonView.getTemplateEngine() >> templateEngine

        jsonView.getViewTemplate() >> new GrailsViewTemplate(JsonView)

        viewHelper
    }
}

@Entity
class Team {
    String name
    Player captain
    List players
    List<String> titles
    static hasMany = [players:Player]
}
@Entity
class Player {
    String name
    static belongsTo = [team:Team]
}

@Entity
class Test {
    String title
    TestAuthor author

    static embedded = ['author']
}

class TestAuthor {
    String name
}

class Test2 {
    String title
    String author
}