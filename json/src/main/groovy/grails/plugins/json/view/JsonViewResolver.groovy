package grails.plugins.json.view

import grails.views.mvc.GenericGroovyTemplateViewResolver
import grails.web.mime.MimeType

/**
 * @author Graeme Rocher
 */
class JsonViewResolver extends GenericGroovyTemplateViewResolver {

    public static final String JSON_VIEW_SUFFIX = ".${JsonTemplate.EXTENSION}"

    JsonViewResolver(String baseClassName = JsonTemplate.name, boolean compileStatic = true) {
        super(new JsonTemplateEngine(baseClassName, compileStatic))
        setSuffix(JSON_VIEW_SUFFIX)
        setContentType(MimeType.JSON.name)
    }
}
