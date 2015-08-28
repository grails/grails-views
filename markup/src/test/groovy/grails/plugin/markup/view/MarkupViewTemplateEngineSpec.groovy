package grails.plugin.markup.view

import spock.lang.Specification

/**
 * Created by graemerocher on 28/08/15.
 */
class MarkupViewTemplateEngineSpec extends Specification {

    void "Test parse markup template"() {
        given:"A template engine"
        def templateEngine = new MarkupViewTemplateEngine()

        when:"A template is parsed"
        def template = templateEngine.createTemplate('''
model {
    Iterable<Map> cars
}
xmlDeclaration()
cars {
   cars.each {
       car(make: it.make, model: it.model)
   }
}
''')

        def writable = template.make(cars: [[make:"Audi", model:"A5"]])

        then:"The writable is of the correct type"
        writable instanceof MarkupTemplate

        when:"The writable writes"

        def writer = new StringWriter()
        writable.writeTo(writer)

        then:"The output is correct"
        writer.toString() == '''<?xml version='1.0'?>
<cars><car make='Audi' model='A5'/></cars>'''
    }
}
