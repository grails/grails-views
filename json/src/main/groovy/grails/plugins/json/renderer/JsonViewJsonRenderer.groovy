package grails.plugins.json.renderer

import grails.core.support.proxy.ProxyHandler
import grails.plugins.json.view.JsonTemplateEngine
import grails.rest.render.RenderContext
import grails.rest.render.Renderer
import grails.rest.render.RendererRegistry
import grails.web.mime.MimeType
import groovy.text.Template
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.plugins.web.rest.render.html.DefaultHtmlRenderer
import org.grails.plugins.web.rest.render.json.DefaultJsonRenderer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors

import javax.annotation.PostConstruct

/**
 * A renderer that will lookup a JSON view if it exists and render it, otherwise fallback to default
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@InheritConstructors
@CompileStatic
class JsonViewJsonRenderer<T> extends DefaultHtmlRenderer<T> {

    final JsonTemplateEngine templateEngine

    final ProxyHandler proxyHandler

    final RendererRegistry rendererRegistry

    final Renderer defaultJsonRenderer

    JsonViewJsonRenderer(Class<T> targetType, JsonTemplateEngine templateEngine, ProxyHandler proxyHandler, RendererRegistry rendererRegistry, Renderer defaultJsonRenderer) {
        super(targetType, MimeType.JSON)
        this.templateEngine = templateEngine
        this.proxyHandler = proxyHandler
        this.rendererRegistry = rendererRegistry
        this.defaultJsonRenderer = defaultJsonRenderer
    }



    @Override
    void render(T object, RenderContext context) {
        final mimeType = context.acceptMimeType ?: mimeTypes[0]
        if (!mimeType.equals(MimeType.ALL)) {
            context.setContentType(mimeType.name)
        }

        String viewName
        if (context.arguments?.view) {
            viewName = context.arguments.view.toString()
        }
        else {
            viewName = context.actionName
        }

        def viewUri = "/${context.controllerName}/${viewName}"
        def template = templateEngine.resolveTemplate(viewUri)
        if(template != null) {

            context.setViewName(viewUri)

            if (object instanceof Errors) {
                Errors errors = (Errors)object
                def target = errors instanceof BeanPropertyBindingResult ? errors.getTarget() : null
                if (target) {
                    applyModel(context, target)
                }
            } else {
                applyModel(context, object)
            }
        }
        else {
            defaultJsonRenderer.render(object, context)
        }
    }



}
