package grails.views.gradle.json

import grails.views.gradle.AbstractGroovyTemplateCompileTask
import groovy.transform.CompileStatic

/**
 * Concrete implementation that compiles JSON templates
 *
 * @author Graeme Rocher
 */
@CompileStatic
class JsonViewCompilerTask extends AbstractGroovyTemplateCompileTask {

    @Override
    String getFileExtension() {
        "gson"
    }

    @Override
    String getScriptBaseName() {
        "grails.plugin.json.view.JsonTemplate"
    }

    @Override
    protected String getCompilerName() {
        "grails.plugin.json.view.JsonViewsCompiler"
    }

    @Override
    void prepareArguments(List<String> arguments) {
        arguments.add(String.valueOf(compileOptions.compileStatic))
    }
}
