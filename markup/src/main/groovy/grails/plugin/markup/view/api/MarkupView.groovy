package grails.plugin.markup.view.api

import grails.artefact.Enhances
import grails.views.Views
import grails.views.api.GrailsView
import grails.views.api.GrailsViewHelper
import grails.views.api.internal.DefaultGrailsViewHelper

/**
 * Extra methods added to markup views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Enhances(Views.TYPE)
trait MarkupView extends GrailsView {
    /**
     * Overrides the default helper with new methods specific to JSON building
     */
    private GrailsViewHelper viewHelper = new DefaultGrailsViewHelper(this)

    @Override
    GrailsViewHelper getG() {
        return viewHelper
    }
}