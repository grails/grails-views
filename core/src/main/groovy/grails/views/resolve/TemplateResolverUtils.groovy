package grails.views.resolve

import grails.util.GrailsNameUtils
import groovy.transform.CompileStatic

/**
 * Utility methods for resolving template names
 *
 * @since 1.1
 * @author Graeme Rocher
 */
@CompileStatic
class TemplateResolverUtils {

    static String shortTemplateNameForClass(Class<?> cls) {
        def propertyName = GrailsNameUtils.getPropertyName(cls)
        propertyName = filterOutProxy(propertyName)
        return "/$propertyName/_$propertyName"
    }

    static String fullTemplateNameForClass(Class<?> cls) {
        def clsSimpleName = getClassSimpleName(cls)
        def templateName = cls.name.replace('.', '/')
        def lastSlash = templateName.lastIndexOf('/')
        def stem = templateName.substring(0, lastSlash)
        return "/$stem/_${GrailsNameUtils.getPropertyName(clsSimpleName)}"
    }

    private static String getClassSimpleName(Class<?> cls) {
        filterOutProxy(cls.simpleName)
    }

    static String filterOutProxy(String className) {
        if (className.contains('$$_jvst')) {
            className = className.substring(0, className.indexOf('_$$'))
        }
        className
    }
}
