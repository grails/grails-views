package grails.views.api.http

/**
 * Allows access to request properties
 */
interface Request {
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

    /**
     * @return The header for the request
     */
    Collection<String> getHeaderNames()

    /**
     * Obtains the value of a header
     *
     * @param name The name of the header
     * @return The value of the header
     */
    String getHeader(String name)

    /**
     * Obtains all the values for the give header
     *
     * @param name The name of the header
     * @return all of the views
     */
    Collection<String> getHeaders(String name)
}