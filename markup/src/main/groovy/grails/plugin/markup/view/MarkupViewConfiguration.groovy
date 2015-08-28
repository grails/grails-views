package grails.plugin.markup.view

import groovy.text.markup.TemplateConfiguration

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class MarkupViewConfiguration extends TemplateConfiguration implements grails.views.TemplateConfiguration {

    boolean compileStatic = true
    boolean enableReloading = false
    String packageName = ""
    String extension = MarkupTemplate.EXTENSION

    MarkupViewConfiguration() {
        setBaseTemplateClass(MarkupTemplate)
    }

    @Override
    boolean isCache() {
        return isCacheTemplates()
    }
}
