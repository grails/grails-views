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
}