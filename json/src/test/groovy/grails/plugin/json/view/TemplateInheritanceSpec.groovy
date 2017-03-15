package grails.plugin.json.view

import grails.plugin.json.view.test.JsonViewTest
import grails.views.ViewException
import spock.lang.Specification

/**
 * Created by graemerocher on 24/05/16.
 */
class TemplateInheritanceSpec extends Specification implements JsonViewTest {

    void "test extending another template"() {
        when:

        def result = render(template:'child2', model:[player: new Player(name: "Cantona")])
        then:
        result.jsonText == '{"_links":{"self":{"href":"http://localhost:8080/player","hreflang":"en","type":"application/hal+json"}},"foo":"bar","name":"Cantona"}'
    }

    void "test extending another template that uses g.render(..)"() {

        when:
        def player = new Player(name: "Cantona")
        player.id = 1L
        def result = render(template:'child3', model:[player: player])
        then:
        result.jsonText == '{"id":1,"name":"Cantona"}'
    }

    void "test extending another template and rendering a JSON block"() {
        when:

        def result = render(template:'child4', model:[player: new Player(name: "Cantona")])
        then:
        result.jsonText == '{"_links":{"self":{"href":"http://localhost:8080/player","hreflang":"en","type":"application/hal+json"}},"foo":"bar","name":"Cantona"}'
    }

    void "test circular rendering is handled"() {
        when:
        render(template:'circular/circular', model:[circular: new Circular(name: "Cantona")])

        then:
        def ex = thrown(ViewException)
        ex.cause instanceof RuntimeException
        ex.cause.message == "Circular view render occurred for view /circular/_circular.gson"
    }

}
