package grails.plugin.json.view.mvc

import grails.core.support.proxy.ProxyHandler
import grails.plugin.json.renderer.JsonViewJsonRenderer
import grails.plugin.json.view.JsonTemplate
import grails.plugin.json.view.JsonTemplateEngine
import grails.plugin.json.view.JsonViewConfiguration
import grails.rest.render.RendererRegistry
import grails.views.TemplateConfiguration
import grails.views.mvc.GenericGroovyTemplateViewResolver
import grails.web.mime.MimeType
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct
/**
 * @author Graeme Rocher
 * @since 1.0
 */
class JsonViewResolver extends GenericGroovyTemplateViewResolver {

    public static final String JSON_VIEW_SUFFIX = ".${JsonTemplate.EXTENSION}"

    @Autowired(required = false)
    ProxyHandler proxyHandler

    @Autowired(required = false)
    RendererRegistry rendererRegistry


    JsonViewResolver(JsonViewConfiguration configuration = new JsonViewConfiguration()) {
        super(new JsonTemplateEngine(configuration))
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
