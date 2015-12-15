package grails.views.api

import grails.artefact.Enhances
import grails.views.Views
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
    Page page

    /**
     * @return The request object
     */
    Request request

    /**
     * Allows access to request properties
     */
    static interface Request {
        /**
         * @return The context path
         */
        String getContextPath()

        /**
         * @return The request method
         */
        String getMethod()

        /**
         * @return The request URI
         */
        String getUri()

        /**
         * @return The request content type
         */
        String getContentType()
        /**
         * @return The request character encoding
         */
        String getCharacterEncoding()
    }
    /**
     * Allows control over the page response (headers, content type, status)
     */
    static interface Page {

        /**
         * Set a response header
         *
         * @param name The name of the header
         * @param value The value of the header
         */
        void header(String name, String value)

        /**
         * Set a single named value header
         * @param nameAndValue The name and value. Example header(foo:"bar")
         */
        void header(Map<String,String> nameAndValue)

        /**
         * Set multiple headers
         *
         * @param namesAndValues The names and values
         */
        void headers(Map<String,String> namesAndValues)

        /**
         * Set the response content type
         *
         * @param contentType
         */
        void contentType(String contentType)

        /**
         * Sets the response encoding
         *
         * @param encoding
         */
        void encoding(String encoding)

        /**
         * Sets the response status
         *
         * @param status The status
         */
        void status(int status)

        /**
         * Sets the response status
         *
         * @param status The status
         */
        void status(int status, String message)

        /**
         * Sets the response status
         *
         * @param status The status
         */
        void status(HttpStatus status)

        /**
         * Sets the response status
         *
         * @param status The status
         */
        void status(HttpStatus status, String message)
    }
}