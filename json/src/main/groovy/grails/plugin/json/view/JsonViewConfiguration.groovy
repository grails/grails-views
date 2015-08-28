package grails.plugin.json.view

import grails.views.GenericViewConfiguration

/**
 * Default configuration for JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
class JsonViewConfiguration extends GenericViewConfiguration {

    JsonViewConfiguration() {
        setExtension(JsonViewTemplate.EXTENSION)
        setCompileStatic(true)
        setBaseTemplateClass(JsonViewTemplate)
    }
}
