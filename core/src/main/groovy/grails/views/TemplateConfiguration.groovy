package grails.views

/**
 * Interface for view configurations
 *
 * @author Graeme Rocher
 * @since 1.0
 */
interface TemplateConfiguration {
    /**
     * @return Should compile statically
     */
    boolean isCompileStatic()

    /**
     * @return Whether reloading is enabled
     */
    boolean isEnableReloading()

    /**
     * @return The package name
     */
    String getPackageName()

    /**
     * @return The file extension
     */
    String getExtension()

    /**
     * @return The template base class
     */
    Class getBaseTemplateClass()

    /**
     * @return Whether to cache
     */
    boolean isCache()
}