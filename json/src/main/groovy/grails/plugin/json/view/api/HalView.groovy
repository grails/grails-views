package grails.plugin.json.view.api

import grails.artefact.Enhances
import grails.plugin.json.view.JsonViewTemplate

/**
 * Extends default view API with additional methods for rendering HAL responses
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Enhances(JsonViewTemplate.TYPE)
trait HalView extends JsonView {

	HalViewHelper hal = new HalViewHelper(this, getG())
}