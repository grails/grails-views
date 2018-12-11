package grails.plugin.json.view

import grails.gorm.annotation.Entity
import grails.plugin.json.view.test.JsonViewTest
import groovy.json.JsonSlurper
import spock.lang.Issue
import spock.lang.Specification

class EmbeddedAssociationsSpec extends Specification implements JsonViewTest, GrailsUnitTest {

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
        result.jsonText.contains '"homeAddress":{"postCode":"12345"}'
        result.jsonText.contains '"name":"Robert"'
        result.jsonText.contains '"nickNames":["Rob","Bob"]'
        result.jsonText.contains '"otherAddresses":[{"postCode":"6789"},{"postCode":"54321"}]'
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
        Map m = new JsonSlurper().parseText(result.jsonText)
        m.containsKey('links')
        m['links'].containsKey('self')
        m['links']['self'] == '/person/2'
        m.containsKey('data')
        m['data'].containsKey('type')
        m['data']['type'] == 'person'
        m['data']['id'] == '2'
        m['data'].containsKey('attributes')
        m['data']['attributes']['homeAddress']['postCode'] == '12345'
        m['data']['attributes']['name'] == 'Robert'
        m['data']['attributes']['nickNames'] == ["Rob","Bob"]
        m['data']['attributes']['otherAddresses'].size() == 2
        m['data']['attributes']['otherAddresses'].collect { it['postCode'] }.contains('6789')
        m['data']['attributes']['otherAddresses'].collect { it['postCode'] }.contains('54321')
    }

    @Issue("https://github.com/grails/grails-views/issues/171")
    void 'test render domain object with embedded associations and include'() {
        given: 'a domain class with embedded associations'
        mappingContext.addPersistentEntities(Person)
        Person p = new Person(name:"Robert")
        p.homeAddress = new Address(postCode: "12345")
        p.otherAddresses = [new Address(postCode: "6789"), new Address(postCode: "54321")]
        p.nickNames = ['Rob','Bob']

        when: 'an instance with embedded associations is rendered'
        def result = render('''
import grails.plugin.json.view.*

model {
    Person person
}
json g.render(person, [includes: ['name', 'homeAddress']])
''', [person:p])

        then: 'the result is correct'
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['homeAddress']['postCode'] == "12345"
        m['name'] == "Robert"
    }

    @Issue("https://github.com/grails/grails-views/issues/171")
    void 'test render domain object with embedded associations and include in json api'() {
        given: 'a domain class with embedded associations'
        mappingContext.addPersistentEntities(Person)
        Person p = new Person(name:"Robert")
        p.id = 4
        p.homeAddress = new Address(postCode: "12345")
        p.otherAddresses = [new Address(postCode: "6789"), new Address(postCode: "54321")]
        p.nickNames = ['Rob','Bob']

        when: 'an instance with embedded associations is rendered'
        def result = render('''
import grails.plugin.json.view.*

model {
    Person person
}
json jsonapi.render(person, [includes: ['name', 'homeAddress']])
''', [person:p])

        then: 'the result is correct'
        Map m = new JsonSlurper().parseText(result.jsonText)
        m['data']['type'] == 'person'
        m['data']['id'] == '4'
        m['data']['attributes']['homeAddress']['postCode'] == '12345'
        m['data']['attributes']['name'] == 'Robert'
        m['links']['self'] == '/person/4'
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