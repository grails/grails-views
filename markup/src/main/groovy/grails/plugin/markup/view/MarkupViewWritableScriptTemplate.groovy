package grails.plugin.markup.view

import grails.views.WritableScript
import grails.views.WritableScriptTemplate
import grails.views.api.GrailsView
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class MarkupViewWritableScriptTemplate extends WritableScriptTemplate {

    MarkupTemplateEngine templateEngine
    MarkupViewConfiguration configuration

    MarkupViewWritableScriptTemplate(Class<? extends GrailsView> templateClass, File sourceFile, MarkupTemplateEngine templateEngine, MarkupViewConfiguration configuration) {
        super(templateClass, sourceFile)
        this.templateEngine = templateEngine
        this.configuration = configuration
    }

    @Override
    Writable make(Map binding) {
        def writableTemplate = templateClass
                .newInstance(templateEngine, binding, Collections.emptyMap(), configuration)
        writableTemplate.setSourceFile(sourceFile)
        return writableTemplate
    }
}
