package grails.plugins.json.view.api

import grails.artefact.Enhances
import grails.plugins.json.view.api.internal.JsonGrailsViewHelper
import grails.views.Views
import grails.views.api.GrailsView
import grails.views.api.GrailsViewHelper

/**
 * Extends default view API with additional methods
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Enhances(Views.TYPE)
trait JsonView extends GrailsView {

    /**
     * Overrides the default helper with new methods specific to JSON building
     */
    private GrailsJsonViewHelper viewHelper = new JsonGrailsViewHelper(this)

    @Override
    GrailsJsonViewHelper getG() {
        return viewHelper
    }
}