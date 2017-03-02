package grails.plugin.json.view.api

import grails.persistence.Entity
import grails.plugin.json.view.test.JsonRenderResult
import grails.plugin.json.view.test.JsonViewTest
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.validation.Validateable
import grails.validation.ValidationErrors
import spock.lang.Specification


@TestMixin(GrailsUnitTestMixin)
class JsonApiSpec extends Specification implements JsonViewTest {
    void setup() {
        mappingContext.addPersistentEntities(Widget, Author, Book)

    }

    void 'test simple case'() {
        given:
            Widget theWidget = new Widget(name: 'One', width: 4, height: 7)
            theWidget.id = 5

        when:
            def result = render('''
import grails.plugin.json.view.api.Widget
model {
    Widget widget
}

json jsonapi.render(widget)
''', [widget: theWidget])

        then:
            result.jsonText == '''{"data":{"type":"widget","id":"5","attributes":{"height":7,"name":"One","width":4}},"links":{"self":"/widget/5"}}'''
    }

    void 'test Relationships'() {
        given:
            Book returnOfTheKing = new Book(
                title: 'The Return of the King',
                author: new Author(name: "J.R.R. Tolkien")
            )
            returnOfTheKing.id = 3
            returnOfTheKing.author.id = 9


        when:
            JsonRenderResult result = render('''
import grails.plugin.json.view.api.Book
model {
    Book book
}

json jsonapi.render(book)
''', [book: returnOfTheKing])

        then: 'The JSON relationships are in place'
            result.jsonText == '{"data":{"type":"book","id":"3","attributes":{"title":"The Return of the King"},"relationships":{"author":{"data":{"type":"author","id":"9"}}}},"links":{"self":"/book/3","related":{"href":"/author/9"}}}'
    }

    void 'test errors'() {
        given:
            SuperHero mutepool = new SuperHero()
            mutepool.name = ""
            mutepool.id = 5
            mutepool.validate()

        when:
            def result = render('''
import grails.plugin.json.view.api.SuperHero
model {
    SuperHero hero
}

json jsonapi.render(hero)
''', [hero: mutepool])

        then:
            result.jsonText == '''{"errors":[{"code":"blank","detail":"Property [name] of class [class grails.plugin.json.view.api.SuperHero] cannot be blank","source":{"object":"grails.plugin.json.view.api.SuperHero","field":"name","rejectedValue":"","bindingError":false}}]}'''
    }

    void 'test jsonapi object'() {
        given:
            Widget theWidget = new Widget(name: 'One', width: 4, height: 7)
            theWidget.id = 5

        when:
            def result = render('''
import grails.plugin.json.view.api.Widget
model {
    Widget widget
}

json jsonapi.render(widget, [jsonApiObject: true])
''', [widget: theWidget])

        then:
            result.jsonText == '''{"jsonapi":{"version":"1.0"},"data":{"type":"widget","id":"5","attributes":{"height":7,"name":"One","width":4}},"links":{"self":"/widget/5"}}'''
    }

    void 'test compound documents object'() {
        given:
            Book returnOfTheKing = new Book(
                title: 'The Return of the King',
                author: new Author(name: "J.R.R. Tolkien")
            )
            returnOfTheKing.id = 3
            returnOfTheKing.author.id = 9


        when:
            JsonRenderResult result = render('''
import grails.plugin.json.view.api.Book
model {
    Book book
}

json jsonapi.render(book, [expand: 'author'])
''', [book: returnOfTheKing])

        then: 'The JSON relationships are in place'
            result.jsonText == '{"data":{"type":"book","id":"3","attributes":{"title":"The Return of the King"},"relationships":{"author":{"data":{"type":"author","id":"9"}}}},"links":{"self":"/book/3","related":{"href":"/author/9"}},"included":[{"type":"author","id":"9","attributes":{"name":"J.R.R. Tolkien"},"links":{"self":"/author/9"}}]}'
    }

    void "test meta object rendering with jsonApiObject"() {
        given:
        Widget theWidget = new Widget(name: 'One', width: 4, height: 7)
        theWidget.id = 5
        def meta = [copyright: "Copyright 2015 Example Corp.",
                    authors: [
                        "Yehuda Katz",
                        "Steve Klabnik"
                    ]]

        when:
        def result = render('''
import grails.plugin.json.view.api.Widget
model {
    Widget widget
    Object meta
}

json jsonapi.render(widget, [jsonApiObject: true, meta: meta])
''', [widget: theWidget, meta: meta])

        then:
        result.jsonText == '''{"jsonapi":{"version":"1.0","meta":{"copyright":"Copyright 2015 Example Corp.","authors":["Yehuda Katz","Steve Klabnik"]}},"data":{"type":"widget","id":"5","attributes":{"height":7,"name":"One","width":4}},"links":{"self":"/widget/5"}}'''
    }

    void "test meta object rendering without jsonApiObject"() {
        given:
        Widget theWidget = new Widget(name: 'One', width: 4, height: 7)
        theWidget.id = 5
        def meta = [copyright: "Copyright 2015 Example Corp.",
                    authors: [
                            "Yehuda Katz",
                            "Steve Klabnik"
                    ]]

        when:
        def result = render('''
import grails.plugin.json.view.api.Widget
model {
    Widget widget
    Object meta
}

json jsonapi.render(widget, [meta: meta])
''', [widget: theWidget, meta: meta])

        then:
        result.jsonText == '''{"meta":{"copyright":"Copyright 2015 Example Corp.","authors":["Yehuda Katz","Steve Klabnik"]},"data":{"type":"widget","id":"5","attributes":{"height":7,"name":"One","width":4}},"links":{"self":"/widget/5"}}'''
    }

}

@Entity
class Widget {
    String name
    int width
    int height
}

@Entity
class Book {
    String title
    Author author
}

@Entity
class Author {
    String name
}

@Entity
class SuperHero implements Validateable {
    String name

    static constraints = {
        name(blank: false)
    }
}

