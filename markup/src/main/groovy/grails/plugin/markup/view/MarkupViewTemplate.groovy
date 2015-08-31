package grails.plugin.markup.view

import grails.views.Views
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
abstract class MarkupViewTemplate extends BaseTemplate implements WritableScript {

    public static final String EXTENSION = "gxml"
    public static final String TYPE = "views.gxml"

    File sourceFile

    MarkupViewTemplate(MarkupTemplateEngine templateEngine, Map model, Map<String, String> modelTypes, TemplateConfiguration configuration) {
        super(templateEngine, model, modelTypes, configuration)
    }

    @Override
    void setBinding(Binding binding) {
        ((Script)this).setBinding(binding)
    }


}
