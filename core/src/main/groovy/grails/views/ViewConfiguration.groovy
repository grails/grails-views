package grails.views

/**
 * Interface for view configurations
 *
 * @author Graeme Rocher
 * @since 1.0
 */
interface ViewConfiguration {
    /**
     * @return Should compile statically
     */
    boolean isCompileStatic()

    /**
     * @return Whether reloading is enabled
     */
    boolean isEnableReloading()

    /**
     * @return Whether to pretty print
     */
    boolean isPrettyPrint()

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

    /**
     * @return Path to the templates
     */
    String getTemplatePath()

    /**
     * @return The packages to automatically import
     */
    String[] getPackageImports()

    /**
     * @return The static imports to automatically import
     */
    String[] getStaticImports()

    /**
     * @return The name of the views module (example json or markup)
     */
    String getViewModuleName()

    /**
     * @return The default encoding to use to render views
     */
    String getEncoding()
}