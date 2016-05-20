package grails.plugin.json.view.api

import grails.artefact.Enhances
import grails.plugin.json.view.JsonViewTemplate
import grails.plugin.json.view.api.internal.DefaultHalViewHelper

/**
 * Extends default view API with additional methods for rendering HAL responses
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Enhances(JsonViewTemplate.TYPE)
trait HalView extends JsonView {

	/**
	 * The HAL view helper
	 */
	HalViewHelper hal = new DefaultHalViewHelper(this, getG())
}