package grails.plugin.json.view

import grails.views.GenericTemplateConfiguration

/**
 * Default configuration for JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
class JsonViewConfiguration extends GenericTemplateConfiguration {

    JsonViewConfiguration() {
        setExtension(JsonTemplate.EXTENSION)
        setCompileStatic(true)
        setBaseTemplateClass(JsonTemplate)
    }
}
