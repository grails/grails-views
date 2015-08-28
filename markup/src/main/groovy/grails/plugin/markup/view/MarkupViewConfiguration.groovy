package grails.plugin.markup.view

import grails.views.ViewConfiguration
import groovy.text.markup.TemplateConfiguration

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class MarkupViewConfiguration extends TemplateConfiguration implements ViewConfiguration {

    boolean compileStatic = true
    boolean enableReloading = false
    String packageName = ""
    String extension = MarkupViewTemplate.EXTENSION

    MarkupViewConfiguration() {
        setBaseTemplateClass(MarkupViewTemplate)
    }

    @Override
    boolean isCache() {
        return isCacheTemplates()
    }
}
