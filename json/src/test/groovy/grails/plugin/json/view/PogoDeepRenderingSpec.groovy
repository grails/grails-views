package grails.plugin.json.view

import groovy.json.JsonSlurper
import spock.lang.Issue
import spock.lang.Specification

/**
 * Created by graemerocher on 13/04/16.
 */
class PogoDeepRenderingSpec extends Specification{

    @Issue('https://github.com/grails/grails-views/issues/18')
    void "Test deep rendering a POGO produces the correct json"() {
        given:"A template that deep renders a POGO"
        def templateEngine = new JsonViewTemplateEngine()
        def template = templateEngine.createTemplate('''
import grails.plugin.json.view.GrandParent2

model {
    GrandParent2 grandParent
}

json g.render(grandParent, [deep: true])
''')

        when:"The template is rendered"
        def writer = new StringWriter()



        def child = new Child2(name: "child")
        def parent = new Parent2(name: "parent", children: [child])
        child.parent = parent
        def grandParent = new GrandParent2(name: "grandParent", children: [parent])
        def writable = template.make(grandParent: grandParent)
        writable.writeTo(writer)
        def jsonStr = writer.toString()
        def json = new JsonSlurper().parseText(jsonStr)
        println jsonStr
        then:"The JSON is correct"
        json.name == 'grandParent'
        json.children[0].name == 'parent'
        json.children[0].children[0].name == 'child'
    }
}


class GrandParent2 {

    String name

    List<Parent2> children
}
class Parent2 {

    String name

    List<Child2> children
}

class Child2 {

    Parent2 parent
    String name

}
