package grails.plugin.json.view.api

import grails.persistence.Entity
import grails.plugin.json.view.test.JsonViewTest
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.json.JsonException
import spock.lang.Ignore
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class JsonApiHandleAssociationsSpec extends Specification implements JsonViewTest {
    void setup() {
        mappingContext.addPersistentEntities(Author, PublishedBook, Publisher)
    }


    @Ignore
    void 'must handle more than one associated objects'() {
        given:
            PublishedBook returnOfTheKing = new PublishedBook(
                    title: 'The Return of the King',
                    author: new Author(name: "J.R.R. Tolkien"),
                    publisher: new Publisher(name: 'George Allen & Unwin')
            )
            returnOfTheKing.id = 3
            returnOfTheKing.author.id = 9
            returnOfTheKing.publisher.id = 81


        when:
            render('''
import grails.plugin.json.view.api.PublishedBook
model {
    PublishedBook book
}

json jsonapi.render(book)
''', [book: returnOfTheKing])

        then: 'should not throw exception'
            notThrown(JsonException)
    }

}


@Entity
class PublishedBook {
    String title
    Author author
    Publisher publisher
}


@Entity
class Publisher {
    String name
}

