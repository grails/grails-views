package grails.plugin.json.view

import grails.plugin.json.view.test.JsonViewTest
import spock.lang.Specification

/**
 * Created by graemerocher on 30/08/2016.
 */
class ExceptionRenderSpec extends Specification implements JsonViewTest {

    void "Test render an exception type"() {

        when:"An exception is rendered"
        def renderResult = render('''
import groovy.transform.*
import grails.plugin.json.view.*

try {
    throw new RuntimeException("bad")
}
catch(Throwable e) {
    json g.render(e)
}

''')

        then:"The exception is rendered"
        renderResult.json.message == 'bad'
        renderResult.json.stacktrace[0] == '6 | JsonView0.run'

    }

}
