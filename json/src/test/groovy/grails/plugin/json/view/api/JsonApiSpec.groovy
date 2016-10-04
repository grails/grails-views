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
            result.jsonText == '''{"data":{"type":"widget","id":"5","attributes":{"height":7,"name":"One","version":null,"width":4}},"links":{"self":"/widget/5"}}'''
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
            result.json
            def relationships = result.json.data.relationships
            relationships.size() == 1
            relationships.author
            relationships.author.data.id
            relationships.author.data.type == "author"

        and: 'the links are in order '
            result.json.links.self == '/book/3'
            result.json.links.related.href == '/author/9'
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

json jsonapi.render(widget, [showJsonApiObject: true])
''', [widget: theWidget])

        then:
            result.jsonText == '''{"jsonapi":{"version":"1.0"},"data":{"type":"widget","id":"5","attributes":{"height":7,"name":"One","version":null,"width":4}},"links":{"self":"/widget/5"}}'''
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

