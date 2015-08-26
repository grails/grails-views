package grails.views.gradle.json

import grails.plugins.json.view.JsonTemplate
import grails.views.gradle.AbstractGroovyTemplateCompileTask
import groovy.transform.CompileStatic

/**
 * Concrete implementation that compiles JSON templates
 *
 * @author Graeme Rocher
 */
@CompileStatic
class JsonTemplateCompilerTask extends AbstractGroovyTemplateCompileTask {
    @Override
    String getFileExtension() {
        JsonTemplate.EXTENSION
    }

    @Override
    String getScriptBaseName() {
        JsonTemplate.name
    }
}
