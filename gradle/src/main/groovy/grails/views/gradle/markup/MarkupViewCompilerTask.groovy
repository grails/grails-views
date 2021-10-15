package grails.views.gradle.markup

import grails.views.gradle.AbstractGroovyTemplateCompileTask
import groovy.transform.CompileStatic
import org.gradle.api.tasks.Input

/**
 * MarkupView compiler task for Gradle
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class MarkupViewCompilerTask extends AbstractGroovyTemplateCompileTask {

    @Input
    @Override
    String getFileExtension() {
        "gml"
    }

    @Input
    @Override
    String getScriptBaseName() {
        "grails.plugin.markup.view.MarkupViewTemplate"
    }

    @Input
    @Override
    protected String getCompilerName() {
        "grails.plugin.markup.view.MarkupViewCompiler"
    }

}
