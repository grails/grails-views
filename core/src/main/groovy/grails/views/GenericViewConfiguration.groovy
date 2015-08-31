package grails.views

import grails.util.BuildSettings
import groovy.transform.CompileStatic
import org.grails.io.support.GrailsResourceUtils

/**
 * Default configuration
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
trait GenericViewConfiguration implements ViewConfiguration {

    boolean enableReloading = false
    String packageName = ""
    boolean compileStatic = true
    String extension
    Class baseTemplateClass
    boolean cache
    String templatePath = BuildSettings.BASE_DIR ? new File(BuildSettings.BASE_DIR, GrailsResourceUtils.VIEWS_DIR_PATH) : "./grails-app/views"
    String[] packageImports = [] as String[]
}
