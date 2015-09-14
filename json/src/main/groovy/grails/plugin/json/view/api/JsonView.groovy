package grails.plugin.json.view.api

import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.view.api.internal.JsonGrailsViewHelper
import grails.plugin.json.view.api.internal.TemplateNamespaceInvoker
import grails.views.api.GrailsView

/**
 * Extends default view API with additional methods
 *
 * @author Graeme Rocher
 * @since 1.0
 */
trait JsonView extends GrailsView {

    /**
     * The {@link StreamingJsonBuilder} instance
     */
    StreamingJsonBuilder json

    /**
     * Overrides the default helper with new methods specific to JSON building
     */
    private GrailsJsonViewHelper viewHelper = new JsonGrailsViewHelper(this)

    /**
     * @return The default view helper
     */
    @Override
    GrailsJsonViewHelper getG() {
        return viewHelper
    }

    /**
     * The template namespace
     */
    TemplateNamespaceInvoker tmpl = new TemplateNamespaceInvoker(viewHelper)
}