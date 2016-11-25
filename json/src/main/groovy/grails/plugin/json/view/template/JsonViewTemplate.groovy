package grails.plugin.json.view.template

import grails.plugin.json.builder.JsonGenerator
import grails.plugin.json.view.JsonViewWritableScript
import grails.views.GrailsViewTemplate
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@CompileStatic
@InheritConstructors
class JsonViewTemplate extends GrailsViewTemplate {

    JsonGenerator generator

    @Override
    Writable make(Map binding) {
        Writable writableTemplate = super.make(binding)
        ((JsonViewWritableScript)writableTemplate).setGenerator(generator)
        writableTemplate
    }
}
