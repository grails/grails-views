package grails.views

import grails.config.Config
import grails.config.ConfigMap
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.support.GrailsApplicationAware
import grails.util.BuildSettings
import grails.util.Environment
import grails.util.Metadata
import groovy.transform.CompileStatic
import org.grails.config.CodeGenConfig
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.io.support.GrailsResourceUtils
import org.springframework.beans.BeanUtils
import org.springframework.core.env.PropertyResolver

import java.beans.PropertyDescriptor
/**
 * Default configuration
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
trait GenericViewConfiguration implements ViewConfiguration, GrailsApplicationAware {

    String encoding = "UTF-8"
    boolean prettyPrint = false
    boolean enableReloading = ViewsEnvironment.isDevelopmentMode()
    String packageName = Metadata.getCurrent().getApplicationName() ?: ""
    boolean compileStatic = true
    String extension
    Class baseTemplateClass
    boolean cache = !Environment.isDevelopmentMode()
    boolean allowResourceExpansion = true
    String templatePath = {
        def current = Environment.current
        def pathToTemplates = current.hasReloadLocation() ? current.reloadLocation : BuildSettings.BASE_DIR?.path
        pathToTemplates ? new File(pathToTemplates, GrailsResourceUtils.VIEWS_DIR_PATH).path : "./grails-app/views"
    }()
    String[] packageImports = ['groovy.transform'] as String[]
    String[] staticImports = ["org.springframework.http.HttpStatus", "org.springframework.http.HttpMethod", "grails.web.http.HttpHeaders"] as String[]

    @Override
    void setGrailsApplication(GrailsApplication grailsApplication) {
        if(grailsApplication != null) {
            def domainArtefacts = grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)
            setPackageImports(
                    findUniquePackages(domainArtefacts)
            )
        }
    }

    public void readConfiguration(File configFile) {
        if(configFile?.exists()) {
            def config = new CodeGenConfig()
            config.loadYml(configFile)
            readConfiguration(config)
        }
    }

    public void readConfiguration(ConfigMap config) {
        def moduleName = viewModuleName
        GroovyObject configObject = (GroovyObject)this
        if (config != null) {
            def descriptors = findViewConfigPropertyDescriptor()
            for (desc in descriptors) {
                if (desc.writeMethod != null) {
                    def propertyName = desc.name
                    def value = config.getProperty("grails.views.${moduleName}.$propertyName", (Class) desc.propertyType)
                    if(value != null) {
                        configObject.setProperty(propertyName, value)
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
