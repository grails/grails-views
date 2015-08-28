package grails.views.gradle.markup

import grails.views.gradle.AbstractGroovyTemplateCompileTask
import groovy.transform.CompileStatic

/**
 * MarkupView compiler task for Gradle
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class MarkupViewCompilerTask extends AbstractGroovyTemplateCompileTask {

    @Override
    String getFileExtension() {
        "gxml"
    }

    @Override
    String getScriptBaseName() {
        "grails.plugin.markup.view.MarkupViewTemplate"
    }

    @Override
    protected String getCompilerName() {
        "grails.plugin.markup.view.MarkupViewCompiler"
    }

    @Override
    void prepareArguments(List<String> arguments) {
        arguments.add(String.valueOf(compileOptions.compileStatic))
    }
}
