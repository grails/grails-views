package grails.plugins.json.view

import groovy.text.Template
import spock.lang.Specification

/**
 * Created by graemerocher on 21/08/15.
 */
class JsonViewTemplateEngineSpec extends Specification {

    void "Test parsing a JSON view template"() {
        when:"An engine is created and a template parsed"
        def templateEngine = new JsonTemplateEngine()
        def template = templateEngine.createTemplate('''
json.person {
    name "bob"
}
''')

        def writer = new StringWriter()
        template.make().writeTo(writer)

        then:"The output is correct"
        writer.toString() == '{"person":{"name":"bob"}}'
    }

    void "Test pretty print a JSON view template"() {
        when:"An engine is created and a template parsed"
        def templateEngine = new JsonTemplateEngine()
        def template = templateEngine.createTemplate('''
json.person {
    name "bob"
}
''')

        def writer = new StringWriter()

        def writable = template.make()
        writable.setPrettyPrint(true)
        writable.writeTo(writer)

        then:"The output is correct"
        writer.toString() == '''{
    "person": {
        "name": "bob"
    }
}'''
    }


    void "Test resolveTemplate method"() {
        when:"A templateEngine is created"
        def templateEngine = new JsonTemplateEngine()
        def template = templateEngine.resolveTemplate("/foo.gson")

        then:"The template exists"
        template != null

        when:"The template is written"
        def writer = new StringWriter()
        template.make().writeTo(writer)

        then:"The output is correct"
        writer.toString() == '{"person":{"name":"bob"}}'


    }
}
