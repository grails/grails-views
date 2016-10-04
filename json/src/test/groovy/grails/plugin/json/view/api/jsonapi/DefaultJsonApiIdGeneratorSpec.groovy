package grails.plugin.json.view.api.jsonapi

import spock.lang.Specification
import spock.lang.Unroll

class DefaultJsonApiIdGeneratorSpec extends Specification {

    @Unroll("DefaultJsonApiIdGenerator.generateId(#subject) == #expectedResult")
    void "test basic ID generation"() {
        given:
            DefaultJsonApiIdGenerator generator = new DefaultJsonApiIdGenerator()

        when:
            String result = generator.generateId(subject)

        then:
            notThrown(Exception)
            result == expectedResult

        where:
            subject      | expectedResult
            null         | null
            [foo: 'bar'] | null
    }

}
