package grails.plugin.json.view.api

import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.views.api.GrailsViewHelper

/**
 * Additional methods specific to JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
interface GrailsJsonViewHelper extends GrailsViewHelper {

    /**
     * The deep parameter
     */
    String DEEP = "deep"

    /**
     * The expand parameter
     */
    String EXPAND = "expand"

    /**
     * Renders a template and returns the output
     *
     * @param arguments The named arguments: 'template', 'collection', 'model', 'var' and 'bean'
     * @return The unescaped JSON
     */
    JsonOutput.JsonUnescaped render(Map arguments)


    /**
     * Renders the given object to JSON, typically a domain class, ignoring lazy and internal properties
     *
     * @param object The object to render
     * @param arguments The supported named arguments: 'includes' or 'excludes' list
     * @param customizer Used to customize the contents
     * @return The unescaped JSON
     */
    JsonOutput.JsonUnescaped render(Object object, Map arguments, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer)
    /**
     * Renders the given object to JSON, typically a domain class, ignoring lazy and internal properties
     *
     * @param object The object to render
     * @param arguments The supported named arguments: 'includes' or 'excludes' list
     * @return The unescaped JSON
     */
    JsonOutput.JsonUnescaped render(Object object, Map arguments)

    /**
     * Renders the given object to JSON, typically a domain class, ignoring lazy and internal properties
     *
     * @param object The object to render
     * @return The unescaped JSON
     */
    JsonOutput.JsonUnescaped render(Object object)

    /**
     * Renders the given object to JSON, typically a domain class, ignoring lazy and internal properties
     *
     * @param object The object to render
     * @param customizer the customizer
     * @return The unescaped JSON
     */
    JsonOutput.JsonUnescaped render(Object object, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer)
}