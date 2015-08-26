package grails.views

/**
 * Interface for views that provider a writer
 *
 * @author Graeme Rocher
 */
interface WriterProvider {

    /**
     * @return The current writer
     */
    Writer getOut()
}