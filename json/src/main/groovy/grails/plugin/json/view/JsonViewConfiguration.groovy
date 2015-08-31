package grails.plugin.json.view

import grails.views.GenericViewConfiguration

/**
 * Default configuration for JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
class JsonViewConfiguration implements GenericViewConfiguration {


    public static final String MODULE_NAME = "json"

    JsonViewConfiguration() {
        setExtension(JsonViewTemplate.EXTENSION)
        setCompileStatic(true)
        setBaseTemplateClass(JsonViewTemplate)
    }

    @Override
    String getViewModuleName() {
         MODULE_NAME
    }
}
