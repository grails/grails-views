package grails.views.mvc

import grails.views.ResolvableGroovyTemplateEngine
import grails.views.api.GrailsView
import grails.views.api.HttpView
import grails.web.mapping.LinkGenerator
import grails.web.mime.MimeType
import groovy.text.Template
import groovy.transform.CompileStatic
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.view.AbstractUrlBasedView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * An implementation of the Spring AbstractUrlBaseView class for ResolvableGroovyTemplateEngine
 *
 * @author Graeme Rocher
 */
@CompileStatic
class GenericGroovyTemplateView extends AbstractUrlBasedView {

    ResolvableGroovyTemplateEngine templateEngine
    LinkGenerator linkGenerator
    LocaleResolver localeResolver
    private String defaultEncoding = "UTF-8"

    void setTemplateEngine(ResolvableGroovyTemplateEngine templateEngine) {
        this.templateEngine = templateEngine
        this.defaultEncoding = templateEngine.viewConfiguration.encoding
    }

    void setLinkGenerator(LinkGenerator linkGenerator) {
        this.linkGenerator = linkGenerator
    }

    void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        def locale = localeResolver?.resolveLocale(httpServletRequest)
        Template template = templateEngine.resolveTemplate(url, locale)
        if(template != null) {

            httpServletResponse.setContentType( getContentType() )
            def writable = template.make(map)
            prepareWritable(writable, httpServletRequest, httpServletResponse, locale)
            def writer = httpServletResponse.writer
            try {
                // set the default encoding
                httpServletResponse.setCharacterEncoding( defaultEncoding )
                // now write the writable
                writable.writeTo(writer)
            } catch (RuntimeException e) {
                if(!httpServletResponse.isCommitted()) {
                    // set back to HTML to errors are rendered correctly
                    httpServletResponse.setContentType(MimeType.HTML.name)
                }
                throw e
            }
            writer.flush()
        }
    }

    protected void prepareWritable(Writable writable, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {
        if (writable instanceof GrailsView) {
            def grailsView = (GrailsView) writable
            grailsView.setLinkGenerator(linkGenerator)
            grailsView.setTemplateEngine(templateEngine)
            grailsView.setLocale(
                locale
            )

            final webRequest = GrailsWebRequest.lookup(httpServletRequest)
            if (webRequest != null) {
                grailsView.setActionName(
                        webRequest.actionName
                )
                grailsView.setControllerName(
                        webRequest.controllerName
                )
            }
        }
        if (writable instanceof HttpView) {
            def page = new HttpViewPage(httpServletResponse)
            ((HttpView) writable).setPage(page)
        }
    }

    @Override
    boolean checkResource(Locale locale) throws Exception {
        return templateEngine?.resolveTemplate(url, locale) != null
    }

    private static class HttpViewPage implements HttpView.Page {
        final HttpServletResponse httpServletResponse

        HttpViewPage(HttpServletResponse httpServletResponse) {
            this.httpServletResponse = httpServletResponse
        }

        @Override
        void header(Map<String, String> nameAndValue) {
            headers(nameAndValue)
        }

        @Override
        void headers(Map<String, String> namesAndValues) {
            for (entry in namesAndValues.entrySet()) {
                header entry.key, entry.value
            }
        }

        @Override
        void header(String name, String value) {
            httpServletResponse.addHeader(name, value)
        }

        @Override
        void contentType(String contentType) {
            httpServletResponse.setContentType(contentType)
        }

        @Override
        void encoding(String encoding) {
            httpServletResponse.setCharacterEncoding(encoding)
        }

        @Override
        void status(int status) {
            httpServletResponse.setStatus(status)
        }

        @Override
        void status(int status, String message) {
            httpServletResponse.sendError(status, message)
        }
    }
}
