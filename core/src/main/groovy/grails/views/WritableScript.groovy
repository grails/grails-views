package grails.views

/**
 * Interface for scripts that are writable
 *
 * @author Graeme Rocher
 * @since 1.0
 */
interface WritableScript extends Writable, WriterProvider {

    /**
     * Obtains the source file
     */
    File getSourceFile()
    /**
     * @param file Sets the source file
     */
    void setSourceFile(File file)

    /**
     * Sets the binding
     *
     * @param binding The binding
     */
    void setBinding(Binding binding)

    /**
     * Runs the script and returns the result
     *
     * @return The result
     */
    Object run()
}