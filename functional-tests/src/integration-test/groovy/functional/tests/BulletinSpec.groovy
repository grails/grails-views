package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Issue

@Integration
@Rollback
class BulletinSpec extends GebSpec {

    @Issue('https://github.com/grails/grails-views/issues/175')
    void 'test render collections with same objects'() {
        given: 'a rest client'
        def builder = new RestBuilder()

        when: 'a GET is issued'
        def resp = builder.get("${baseUrl}bulletin")
        def json = resp.json

        then: 'The REST resource is retrieved and the correct JSON is returned'
        resp.status == 200
        json.content == 'Hi everyone!'

        and: 'the username is the same as the publicId'
        json.targetUsers.size() == 2
        json.targetUsers.every { it.username == it.publicId }
        json.contactUsers.size() == 3
        json.contactUsers.every { it.username == it.publicId }
    }
}
