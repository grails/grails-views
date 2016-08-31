package grails.plugin.json.view

import grails.views.GenericViewConfiguration
import grails.web.mime.MimeType
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Default configuration for JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@ConfigurationProperties('grails.views.json')
class JsonViewConfiguration implements GenericViewConfiguration {

    public static final String MODULE_NAME = "json"

    List<String> mimeTypes = [MimeType.JSON.name, MimeType.HAL_JSON.name]

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
