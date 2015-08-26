package grails.views.api

import grails.artefact.Enhances
import grails.views.ResolvableGroovyTemplateEngine
import grails.views.Views
import grails.views.WriterProvider
import grails.views.api.internal.DefaultGrailsViewHelper
import grails.web.mapping.LinkGenerator
import groovy.transform.CompileStatic
import org.springframework.context.MessageSource

/**
 * A trait for all view types to extend to add methods to generate links, render other templates and so on
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
trait GrailsView extends View implements WriterProvider {

    LinkGenerator linkGenerator

    ResolvableGroovyTemplateEngine templateEngine

    MessageSource messageSource

    /**
     * @return The current controller name
     */
    String controllerName
    /**
     * @return The current action name
     */
    String actionName


    private GrailsViewHelper viewHelper = new DefaultGrailsViewHelper(this)

    GrailsViewHelper getG() {
        return this.viewHelper
    }
}