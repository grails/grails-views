package grails.plugin.markup.view

import grails.util.BuildSettings
import grails.views.ViewConfiguration
import groovy.text.markup.TemplateConfiguration
import org.grails.io.support.GrailsResourceUtils

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class MarkupViewConfiguration extends TemplateConfiguration implements ViewConfiguration {

    boolean compileStatic = true
    boolean enableReloading = false
    String packageName = ""
    String extension = MarkupViewTemplate.EXTENSION
    String templatePath = BuildSettings.BASE_DIR ? new File(BuildSettings.BASE_DIR, GrailsResourceUtils.VIEWS_DIR_PATH) : "./grails-app/views"

    MarkupViewConfiguration() {
        setBaseTemplateClass(MarkupViewTemplate)
    }

    @Override
    boolean isCache() {
        return isCacheTemplates()
    }
}
