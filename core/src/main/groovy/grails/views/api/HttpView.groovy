package grails.views.api

import grails.artefact.Enhances
import grails.views.Views
import grails.views.api.http.Request
import grails.views.api.http.Response
import org.springframework.http.HttpStatus

/**
 * A view that is rendered in the context of an HTTP request
 *
 * @author Graeme Rocher
 * @since 1.0
 */
trait HttpView extends View {

    /**
     * @return The page object
     */
    Response response

    /**
     * @return The request object
     */
    Request request

    /**
     * @return The same as {@link #getResponse()}
     */
    Request getPage() {
        request
    }

}