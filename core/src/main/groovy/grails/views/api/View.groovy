package grails.views.api

/**
 * @author Graeme Rocher
 * @since 1.0
 */
trait View {

    /**
     * The locale of the view
     */
    Locale locale = Locale.ENGLISH

    /**
     * The output stream
     */
    Writer out
}