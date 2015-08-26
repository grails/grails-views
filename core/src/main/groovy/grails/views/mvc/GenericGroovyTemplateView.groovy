package grails.views.mvc

import grails.views.ResolvableGroovyTemplateEngine
import grails.views.api.GrailsView
import grails.views.api.HttpView
import grails.web.mapping.LinkGenerator
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

    @Override
    protected void renderMergedOutputModel(Map<String, Object> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        Template template = templateEngine.resolveTemplate(url)
        if(template != null) {

            httpServletResponse.setContentType( getContentType() )
            def writable = template.make(map)
            prepareWritable(writable, httpServletRequest, httpServletResponse)
            def writer = httpServletResponse.writer
            writable.writeTo(writer)
            writer.flush()
        }
    }

    protected void prepareWritable(Writable writable, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (writable instanceof GrailsView) {
            def grailsView = (GrailsView) writable
            grailsView.setLinkGenerator(linkGenerator)
            grailsView.setTemplateEngine(templateEngine)
            grailsView.setLocale(
                localeResolver?.resolveLocale(httpServletRequest)
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
            ((HttpView) writable).setPage(
                    new HttpView.Page() {

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
            )
        }
    }

    @Override
    boolean checkResource(Locale locale) throws Exception {
        return templateEngine?.resolveTemplate(url) != null
    }
}
