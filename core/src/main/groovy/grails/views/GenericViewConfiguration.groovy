package grails.views

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.support.GrailsApplicationAware
import grails.util.BuildSettings
import grails.util.Environment
import grails.util.Metadata
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.io.support.GrailsResourceUtils
import org.springframework.beans.BeanUtils

import java.beans.PropertyDescriptor
/**
 * Default configuration
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
trait GenericViewConfiguration implements ViewConfiguration, GrailsApplicationAware {

    boolean enableReloading = ViewsEnvironment.isDevelopmentMode()
    String packageName = Metadata.getCurrent().getApplicationName() ?: ""
    boolean compileStatic = true
    String extension
    Class baseTemplateClass
    boolean cache = !Environment.isDevelopmentMode()
    String templatePath = {
        def current = Environment.current
        def pathToTemplates = current.hasReloadLocation() ? current.reloadLocation : BuildSettings.BASE_DIR?.path
        pathToTemplates ? new File(pathToTemplates, GrailsResourceUtils.VIEWS_DIR_PATH).path : "./grails-app/views"
    }()
    String[] packageImports = [] as String[]

    @Override
    void setGrailsApplication(GrailsApplication grailsApplication) {
        if(grailsApplication != null) {
            def domainArtefacts = grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)
            setPackageImports(
                    findUniquePackages(domainArtefacts)
            )
            def config = grailsApplication.getConfig()
            if(config != null) {
                def descriptors = findViewConfigPropertyDescriptor()
                def self = (GroovyObject) this
                def moduleName = viewModuleName
                for(desc in descriptors) {
                    if( desc.writeMethod != null ) {
                        def propertyName = desc.name
                        def value = config.getProperty("grails.views.${moduleName}.$propertyName", (Class)desc.propertyType, self.getProperty(propertyName))
                        self.setProperty(propertyName, value)
                    }
                }
            }
        }
    }

    String[] findUniquePackages(GrailsClass[] grailsClasses) {
        Set packages = []
        for (GrailsClass cls : grailsClasses) {
            packages << cls.packageName
        }
        packages as String[]
    }

    PropertyDescriptor[] findViewConfigPropertyDescriptor() {
        BeanUtils.getPropertyDescriptors(ViewConfiguration)
    }
}
