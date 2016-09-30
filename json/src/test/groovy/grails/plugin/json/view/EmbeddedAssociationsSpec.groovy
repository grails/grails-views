package grails.plugin.json.view

import grails.gorm.annotation.Entity
import grails.plugin.json.view.test.JsonViewTest
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class EmbeddedAssociationsSpec extends Specification implements JsonViewTest {

    void "Test render domain object with embedded associations"() {
        given:"A domain class with embedded associations"
        mappingContext.addPersistentEntities(Person)
        Person p = new Person(name:"Robert")
        p.homeAddress = new Address(postCode: "12345")
        p.otherAddresses = [new Address(postCode: "6789"), new Address(postCode: "54321")]
        p.nickNames = ['Rob','Bob']

        when:"A an instance with embedded assocations is rendered"
        def result = render('''
import grails.plugin.json.view.*

model {
    Person person
}
json g.render(person)
''', [person:p])

        then:"The result is correct"
        result.jsonText == '{"homeAddress":{"postCode":"12345"},"name":"Robert","nickNames":["Rob","Bob"],"otherAddresses":[{"postCode":"6789"},{"postCode":"54321"}]}'
    }

    void "Test render domain object with embedded associations in json api"() {
        given:"A domain class with embedded associations"
        mappingContext.addPersistentEntities(Person)
        Person p = new Person(name:"Robert")
        p.id = 2
        p.homeAddress = new Address(postCode: "12345")
        p.otherAddresses = [new Address(postCode: "6789"), new Address(postCode: "54321")]
        p.nickNames = ['Rob','Bob']

        when:"A an instance with embedded assocations is rendered"
        def result = render('''
import grails.plugin.json.view.*

model {
    Person person
}
json jsonapi.render(person)
''', [person:p])

        then:"The result is correct"
        result.jsonText == '''{"data":{"type":"person","id":"2","attributes":{"homeAddress":{"postCode":"12345"},"name":"Robert","nickNames":["Rob","Bob"],"otherAddresses":[{"postCode":"6789"},{"postCode":"54321"}],"version":null}},"links":{"self":"/person/2"}}'''

    }
}


@Entity
class Person {
    String name
    Address homeAddress
    List<Address> otherAddresses = []
    List<String> nickNames = []

    static embedded = ['homeAddress', 'otherAddresses']
}

class Address {
    String postCode
}