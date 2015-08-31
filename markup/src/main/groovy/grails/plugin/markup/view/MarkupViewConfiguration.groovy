package grails.plugin.markup.view

import grails.util.Environment
import grails.views.GenericViewConfiguration
import groovy.text.markup.TemplateConfiguration

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class MarkupViewConfiguration extends TemplateConfiguration implements GenericViewConfiguration {

    MarkupViewConfiguration() {
        setExtension(MarkupViewTemplate.EXTENSION)
        setBaseTemplateClass(MarkupViewTemplate)
        setCacheTemplates( !Environment.isDevelopmentMode() )
    }

    @Override
    boolean isCache() {
        return isCacheTemplates()
    }
}
