package grails.plugin.json.view.api

import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder

/**
 * @author Graeme Rocher
 * @since 1.1.0
 */
interface HalViewHelper {

    /**
     * Same as {@link GrailsJsonViewHelper#render(java.lang.Object, java.util.Map, groovy.lang.Closure)} but renders HAL links too
     */
    JsonOutput.JsonUnescaped render(Object object, Map arguments, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer)

    /**
     * Same as {@link GrailsJsonViewHelper#render(java.lang.Object, java.util.Map)} but renders HAL links too
     */
    JsonOutput.JsonUnescaped render(Object object, Map arguments)
    /**
     * Same as {@link GrailsJsonViewHelper#render(java.lang.Object, java.util.Map, groovy.lang.Closure)} but renders HAL links too
     */
    JsonOutput.JsonUnescaped render(Object object, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer )

    /**
     * Same as {@link GrailsJsonViewHelper#render(java.lang.Object)} but renders HAL links too
     */
    JsonOutput.JsonUnescaped render(Object object)
    /**
     * @param name Sets the HAL response type
     */
    void type(String name)
    /**
     * Define the hal links
     *
     * @param callable The closure
     */
    void links(Closure callable)

    /**
     * Creates HAL links for the given object
     *
     * @param object The object to create links for
     */
    void links(Object object, String contentType)

    /**
     * Creates HAL links for the given object
     *
     * @param object The object to create links for
     */
    void links(Object object)

    /**
     * Pagination support which outputs hal links to the resulting pages
     *
     * @param object The object to create links for
     * @param total The total number of objects to be paginated
     */
    void paginate(Object object, Integer total)

    /**
     * Pagination support which outputs hal links to the resulting pages
     *
     * @param object The object to create links for
     * @param total The total number of objects to be paginated
     * @param offset The numerical offset where the page starts (defaults to 0)
     */
    void paginate(Object object, Integer total, Integer offset)

    /**
     * Pagination support which outputs hal links to the resulting pages
     *
     * @param object The object to create links for
     * @param total The total number of objects to be paginated
     * @param offset The numerical offset where the page starts (defaults to 0)
     * @param max The maximum number of objects to be shown (defaults to 10)
     */
    void paginate(Object object, Integer total, Integer offset, Integer max)

    /**
     * Pagination support which outputs hal links to the resulting pages
     *
     * @param object The object to create links for
     * @param total The total number of objects to be paginated
     * @param offset The numerical offset where the page starts (defaults to 0)
     * @param max The maximum number of objects to be shown (defaults to 10)
     * @param sort The field to sort on (defaults to null)
     */
    void paginate(Object object, Integer total, Integer offset, Integer max,  String sort)

    /**
     * Pagination support which outputs hal links to the resulting pages
     *
     * @param object The object to create links for
     * @param total The total number of objects to be paginated
     * @param offset The numerical offset where the page starts (defaults to 0)
     * @param max The maximum number of objects to be shown (defaults to 10)
     * @param sort The field to sort on (defaults to null)
     * @param order The order in which the results are to be sorted eg: DESC or ASC
     */
    void paginate(Object object, Integer total, Integer offset, Integer max,  String sort, String order)


    /**
     * Outputs a HAL embedded entry for the given closure
     *
     * @param callable The callable
     */
    void embedded(@DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure callable)


    /**
     * Outputs a HAL embedded entry for the content type and closure
     *
     * @param contentType The content type
     * @param callable The callable
     */
    void embedded(String contentType, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure callable)
}