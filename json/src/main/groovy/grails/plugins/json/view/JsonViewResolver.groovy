package grails.plugins.json.view

import grails.core.support.proxy.ProxyHandler
import grails.plugins.json.renderer.JsonViewJsonRenderer
import grails.rest.render.RendererRegistry
import grails.views.mvc.GenericGroovyTemplateViewResolver
import grails.web.mime.MimeType
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct
/**
 * @author Graeme Rocher
 */
class JsonViewResolver extends GenericGroovyTemplateViewResolver {

    public static final String JSON_VIEW_SUFFIX = ".${JsonTemplate.EXTENSION}"

    @Autowired(required = false)
    ProxyHandler proxyHandler

    @Autowired(required = false)
    RendererRegistry rendererRegistry


    JsonViewResolver(String baseClassName = JsonTemplate.name, boolean compileStatic = true) {
        super(new JsonTemplateEngine(baseClassName, compileStatic))
        setSuffix(JSON_VIEW_SUFFIX)
        setContentType(MimeType.JSON.name)
    }

    JsonViewResolver(JsonTemplateEngine templateEngine) {
        super(templateEngine)
    }

    @PostConstruct
    void initialize() {
        if(rendererRegistry != null) {
            def defaultJsonRenderer = rendererRegistry.findRenderer(MimeType.JSON, Object.class)

            rendererRegistry.addDefaultRenderer(
                    new JsonViewJsonRenderer<Object>(Object.class, this , proxyHandler, rendererRegistry, defaultJsonRenderer)
            )
        }
    }
}
