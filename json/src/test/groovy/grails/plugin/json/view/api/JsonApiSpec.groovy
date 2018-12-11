package grails.plugin.json.view.api

import grails.persistence.Entity
import grails.plugin.json.view.test.JsonRenderResult
import grails.plugin.json.view.test.JsonViewTest
import grails.validation.Validateable
import grails.validation.ValidationErrors
import groovy.json.JsonSlurper
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class JsonApiSpec extends Specification implements JsonViewTest, GrailsUnitTest {
    void setup() {
        mappingContext.addPersistentEntities(Widget, Author, Book, ResearchPaper)
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
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['data']['type'] == 'widget'
        m['data']['id'] == '5'
        m['data']['attributes']['height'] == 7
        m['data']['attributes']['name'] == "One"
        m['data']['attributes']['width'] == 4
        m['links']['self'] == "/widget/5"
    }

    void 'test Relationships - hasOne'() {
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
            result.jsonText == '{"data":{"type":"book","id":"3","attributes":{"title":"The Return of the King"},"relationships":{"author":{"links":{"self":"/author/9"},"data":{"type":"author","id":"9"}}}},"links":{"self":"/book/3"}}'
    }

    void 'test Relationships - multiple'() {
        given:
        ResearchPaper returnOfTheKing = new ResearchPaper(
                title: 'The Return of the King',
                leadAuthor: new Author(name: "J.R.R. Tolkien"),
                coAuthor: new Author(name: "Sally Jones"),
                subAuthors: [new Author(name: "Will"), new Author(name: "Smith")]
        )
        returnOfTheKing.id = 3
        returnOfTheKing.leadAuthor.id = 9
        returnOfTheKing.coAuthor.id = 10
        returnOfTheKing.subAuthors[0].id = 12
        returnOfTheKing.subAuthors[1].id = 13


        when:
        JsonRenderResult result = render('''
import grails.plugin.json.view.api.ResearchPaper
model {
    ResearchPaper researchPaper
}

json jsonapi.render(researchPaper)
''', [researchPaper: returnOfTheKing])

        then: 'The JSON relationships are in place'
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['data']['type'] == 'researchPaper'
        m['data']['id'] == '3'
        m['data']['attributes']['title'] == 'The Return of the King'
        m['data']['relationships']['coAuthor']['links']['self'] == "/author/10"
        m['data']['relationships']['coAuthor']['data']['type'] == "author"
        m['data']['relationships']['coAuthor']['data']['id'] == "10"
        m['data']['relationships']['leadAuthor']['links']['self'] == "/author/9"
        m['data']['relationships']['leadAuthor']['data']['type'] == "author"
        m['data']['relationships']['leadAuthor']['data']['id'] == "9"
        m['data']['relationships']['subAuthors']['data'].size() == 2
        m['data']['relationships']['subAuthors']['data'].any { it['type'] == "author" && it['id'] == "12" }
        m['data']['relationships']['subAuthors']['data'].any { it['type'] == "author" && it['id'] == "13" }
        m['links']['self'] == "/researchPaper/3"
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
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['errors'][0]['code'] == 'blank'
        m['errors'][0]['detail'] == 'Property [name] of class [class grails.plugin.json.view.api.SuperHero] cannot be blank'
        m['errors'][0]['source']['object'] == 'grails.plugin.json.view.api.SuperHero'
        m['errors'][0]['source']['field'] == 'name'
        m['errors'][0]['source']['rejectedValue'] == ''
        m['errors'][0]['source']['bindingError'] == false
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
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['jsonapi']['version'] == '1.0'
        m['data']['type'] == 'widget'
        m['data']['id'] == '5'
        m['data']['attributes']['height'] == 7
        m['data']['attributes']['name'] == 'One'
        m['data']['attributes']['width'] == 4
        m['links']['self'] == '/widget/5'
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
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['data']['type'] == 'book'
        m['data']['id'] == '3'
        m['data']['attributes']['title'] == 'The Return of the King'
        m['data']['relationships']['author']['links']['self'] == '/author/9'
        m['data']['relationships']['author']['data']['type'] == 'author'
        m['data']['relationships']['author']['data']['id'] == '9'
        m['links']['self'] == '/book/3'
        m['included'][0]['type'] == 'author'
        m['included'][0]['id'] == '9'
        m['included'][0]['attributes']['name'] == 'J.R.R. Tolkien'
        m['included'][0]['links']['self'] == '/author/9'
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
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['jsonapi']['version'] == '1.0'
        m['jsonapi']['meta']['copyright'] == 'Copyright 2015 Example Corp.'
        m['jsonapi']['meta']['authors'] == ['Yehuda Katz', 'Steve Klabnik']
        m['data']['type'] == 'widget'
        m['data']['id'] == '5'
        m['data']['attributes']['height'] == 7
        m['data']['attributes']['name'] == 'One'
        m['data']['attributes']['width'] == 4
        m['links']['self'] == '/widget/5'
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
        Map m = new JsonSlurper().parseText(result.jsonText)
        !m.containsKey('jsonapi')
        m['meta']['copyright'] == 'Copyright 2015 Example Corp.'
        m['meta']['authors'] == ['Yehuda Katz', 'Steve Klabnik']
        m['data']['type'] == 'widget'
        m['data']['id'] == '5'
        m['data']['attributes']['height'] == 7
        m['data']['attributes']['name'] == 'One'
        m['data']['attributes']['width'] == 4
        m['links']['self'] == '/widget/5'
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
class ResearchPaper {
    String title
    static hasMany = [subAuthors: Author]
    Author leadAuthor
    Author coAuthor
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

