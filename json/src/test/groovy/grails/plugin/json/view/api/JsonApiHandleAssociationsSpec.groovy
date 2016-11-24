package grails.plugin.json.view.api

import grails.persistence.Entity
import grails.plugin.json.view.test.JsonRenderResult
import grails.plugin.json.view.test.JsonViewTest
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.json.JsonException
import spock.lang.Ignore
import spock.lang.Specification

/**
 * This test shows that all the related hrefs are produced in the jsonText, but they are overwritten when parsed to json.
 * Albeit valid json it clearly is not a desirable solution.
 *
 * I would suggest that the 'related' section be removed (it only MAY exist anyway)
 * and the 'relationships' section be expanded with 'links.self' values, as specified by the test.
 *
 * Links:
 * SO on duplicate keys in JSON syntax: http://stackoverflow.com/questions/21832701/does-json-syntax-allow-duplicate-keys-in-an-object/23195243#23195243
 * JSONAPI Recommendations on Relationship URLs and Related Resource URLs: http://jsonapi.org/recommendations/#urls-relationships
 */
@TestMixin(GrailsUnitTestMixin)
class JsonApiHandleAssociationsSpec extends Specification implements JsonViewTest {

    PublishedBook returnOfTheKing

    void setup() {
        mappingContext.addPersistentEntities(Author, PublishedBook, Publisher)

        returnOfTheKing = new PublishedBook(
                title: 'The Return of the King',
                author: new Author(name: "J.R.R. Tolkien"),
                publisher: new Publisher(name: 'George Allen & Unwin')
        )
        returnOfTheKing.id = 3
        returnOfTheKing.author.id = 9
        returnOfTheKing.publisher.id = 81
    }


    void "all but last related 'href' in links are overwritten"() {
        when:
            JsonRenderResult result = render('''
import grails.plugin.json.view.api.PublishedBook
model {
    PublishedBook book
}

json jsonapi.render(book)
''', [book: returnOfTheKing])

        then:
            result.jsonText.contains('"links":{"self":"/publishedBook/3","related":{"href":"/author/9","href":"/publisher/81"}}')

        and: 'self is ok'
            result.json.links.self == '/publishedBook/3'

        and: 'ERROR: first of the href is overwritten'
            result.json.links.related.size() != 2
            result.json.links.related.href == "/publisher/81"
    }


    @Ignore('Currently likns are not produced in the relationships section')
    void 'relationships should contain links to self'() {
        when:
            JsonRenderResult result = render('''
import grails.plugin.json.view.api.PublishedBook
model {
    PublishedBook book
}

json jsonapi.render(book)
''', [book: returnOfTheKing])

        then:
            result.json
            def relationships = result.json.data.relationships
            relationships.size() == 2

        and:
            relationships.author
            with(relationships.author) {
                data.id == '9'
                data.type == "author"
                links
                links.self == "/author/9"
            }

        and:
            relationships.publisher
            with(relationships.publisher) {
                data.id == '81'
                data.type == "publisher"
                links
                links.self == "/publisher/9"
            }
    }


    void 'more than one associated objects should produce valid JSON'() {
        when:
            JsonRenderResult result = render('''
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

