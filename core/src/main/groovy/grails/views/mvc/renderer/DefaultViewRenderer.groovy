package grails.views.mvc.renderer

import grails.core.support.proxy.ProxyHandler
import grails.rest.render.RenderContext
import grails.rest.render.Renderer
import grails.rest.render.RendererRegistry
import grails.web.mime.MimeType
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.plugins.web.rest.render.ServletRenderContext
import org.grails.plugins.web.rest.render.html.DefaultHtmlRenderer
import org.springframework.web.servlet.ViewResolver

/**
 * A renderer implementation that looks up a view from the ViewResolver
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@InheritConstructors
@CompileStatic
abstract class DefaultViewRenderer<T> extends DefaultHtmlRenderer<T> {
    final ViewResolver viewResolver

    final ProxyHandler proxyHandler

    final RendererRegistry rendererRegistry

    final Renderer defaultRenderer

    DefaultViewRenderer(Class<T> targetType, MimeType mimeType, ViewResolver viewResolver, ProxyHandler proxyHandler, RendererRegistry rendererRegistry, Renderer defaultRenderer) {
        super(targetType,mimeType)
        this.viewResolver = viewResolver
        this.proxyHandler = proxyHandler
        this.rendererRegistry = rendererRegistry
        this.defaultRenderer = defaultRenderer
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
        def view = viewResolver.resolveViewName(viewUri, context.locale)
        if(view != null) {
            Map<String, Object> model = [(resolveModelVariableName(object)): object]

            def webRequest = ((ServletRenderContext) context).getWebRequest()
            view.render(model, webRequest.currentRequest, webRequest.currentResponse)
        }
        else {
            defaultRenderer.render(object, context)
        }
    }
}
