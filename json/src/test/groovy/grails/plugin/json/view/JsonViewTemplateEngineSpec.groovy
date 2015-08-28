package grails.plugin.json.view

import grails.views.ViewCompilationException
import spock.lang.Specification

/**
 * Created by graemerocher on 21/08/15.
 */
class JsonViewTemplateEngineSpec extends Specification {

    void "Test static compilation"() {
        when:"An engine is created and a template parsed"
        def templateEngine = new JsonTemplateEngine()
        def template = templateEngine.createTemplate('''
model {
    URL url
}
json.site {
    protocol url.protocol
}
''')

        def writer = new StringWriter()
        template.make(url: new URL("http://foo.com")).writeTo(writer)

        then:"The output is correct"
        writer.toString() == '{"site":{"protocol":"http"}}'

        when:"A template is compiled with a compilation error"
        template = templateEngine.createTemplate('''
model {
    URL url
}
json.site {
    protocol url.frotocol
}
''')
        writer = new StringWriter()
        template.make(url: new URL("http://foo.com")).writeTo(writer)

        then:"A compilation error is thrown"
        thrown ViewCompilationException
    }

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
