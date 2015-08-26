package grails.plugins.json.view.api

import grails.plugins.json.builder.JsonOutput
import grails.views.api.GrailsViewHelper

/**
 * Additional methods specific to JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
interface GrailsJsonViewHelper extends GrailsViewHelper{

    /**
     * Renders a template and returns the output
     *
     * @param arguments The named arguments: 'template', 'collection', 'model', 'var' and 'bean'
     * @return The unescaped JSON
     */
    JsonOutput.JsonUnescaped render(Map arguments)
}