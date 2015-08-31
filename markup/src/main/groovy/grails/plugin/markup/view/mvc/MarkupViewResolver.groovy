package grails.plugin.markup.view.mvc

import grails.core.support.proxy.ProxyHandler
import grails.plugin.markup.view.MarkupViewConfiguration
import grails.plugin.markup.view.MarkupViewTemplateEngine
import grails.plugin.markup.view.renderer.MarkupViewXmlRenderer
import grails.rest.render.RendererRegistry
import grails.views.mvc.GenericGroovyTemplateViewResolver
import grails.web.mime.MimeType
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class MarkupViewResolver extends GenericGroovyTemplateViewResolver {


    @Autowired(required = false)
    ProxyHandler proxyHandler

    @Autowired(required = false)
    RendererRegistry rendererRegistry


    MarkupViewResolver(MarkupViewConfiguration configuration) {
        super(new MarkupViewTemplateEngine(configuration))
        setSuffix(".$configuration.extension")
        setContentType(MimeType.XML.name)
    }

    MarkupViewResolver(MarkupViewTemplateEngine templateEngine) {
        super(templateEngine)
    }

    @PostConstruct
    void initialize() {
        if(rendererRegistry != null) {
            def defaultXmlRenderer = rendererRegistry.findRenderer(MimeType.XML, Object.class)

            rendererRegistry.addDefaultRenderer(
                    new MarkupViewXmlRenderer<Object>(Object.class, this , proxyHandler, rendererRegistry, defaultXmlRenderer)
            )
        }
    }
}
