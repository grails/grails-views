package grails.plugin.markup.view

import grails.views.WritableScript
import groovy.text.markup.BaseTemplate
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

/**
 * Base class for markup engine templates
 *
 * @author Graeme Rocher
 * @since 1.0
 */
abstract class MarkupTemplate extends BaseTemplate implements WritableScript {

    public static final String EXTENSION = "gxml"

    File sourceFile

    MarkupTemplate(MarkupTemplateEngine templateEngine, Map model, Map<String, String> modelTypes, TemplateConfiguration configuration) {
        super(templateEngine, model, modelTypes, configuration)
    }

    @Override
    void setBinding(Binding binding) {
        ((Script)this).setBinding(binding)
    }


}
