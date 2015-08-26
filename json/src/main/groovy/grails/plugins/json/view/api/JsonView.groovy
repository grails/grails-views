package grails.plugins.json.view.api

import grails.artefact.Enhances
import grails.plugins.json.view.api.internal.JsonGrailsViewHelper
import grails.views.Views
import grails.views.api.GrailsView

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
    GrailsJsonViewHelper g = new JsonGrailsViewHelper(this)
}