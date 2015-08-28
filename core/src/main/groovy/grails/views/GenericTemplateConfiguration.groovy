package grails.views

/**
 * Default configuration
 *
 * @author Graeme Rocher
 * @since 1.0
 */
class GenericTemplateConfiguration implements TemplateConfiguration {

    boolean enableReloading
    String packageName
    boolean compileStatic = true
    String extension
    Class baseTemplateClass
    boolean cache
}
