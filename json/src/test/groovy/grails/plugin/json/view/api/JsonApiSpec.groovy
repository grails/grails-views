package grails.plugin.json.view.api

import grails.persistence.Entity
import grails.plugin.json.view.Team
import grails.plugin.json.view.test.JsonRenderResult
import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

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
            result.jsonText == '''{"data":{"type":"widget","id":"/widget/5","attributes":{"height":7,"name":"One","version":null,"width":4}}}'''
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

        then:
            result.json
            def relationships = result.json.data.relationships
            relationships.size() == 1
            relationships.author
            relationships.author.data.id
            relationships.author.data.type == "author"
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

