package grails.views.gradle.json

import grails.views.gradle.AbstractGroovyTemplateCompileTask
import groovy.transform.CompileStatic
import org.gradle.api.tasks.Input

/**
 * Concrete implementation that compiles JSON templates
 *
 * @author Graeme Rocher
 */
@CompileStatic
class JsonViewCompilerTask extends AbstractGroovyTemplateCompileTask {

    @Input
    @Override
    String getFileExtension() {
        "gson"
    }

    @Input
    @Override
    String getScriptBaseName() {
        "grails.plugin.json.view.JsonViewTemplate"
    }

    @Input
    @Override
    protected String getCompilerName() {
        "grails.plugin.json.view.JsonViewCompiler"
    }

}
