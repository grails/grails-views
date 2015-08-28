package grails.plugin.json.view

import grails.plugins.*
import grails.util.Metadata
import grails.views.ViewsEnvironment

class JsonViewGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.0 > *"

    def title = "JSON View" // Headline display name of the plugin
    def author = "Graeme Rocher"
    def authorEmail = "graeme.rocher@gmail.com"
    def description = '''\
A plugin that allows rendering of JSON views
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://github.com/grails/grails-views"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "OCI", url: "http://www.ociweb.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Graeme Rocher", email: "graeme.rocher@gmail.com" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "Github", url: "http://github.com/grails/grails-views/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "http://github.com/grails/grails-views" ]

    Closure doWithSpring() { {->
            String scriptBaseClass = config.getProperty(JsonTemplateEngine.VIEW_BASE_CLASS, JsonTemplate.name)
            boolean compileStatic = config.getProperty(JsonTemplateEngine.COMPILE_STATIC, Boolean, true)
            jsonViewResolver(JsonViewResolver, scriptBaseClass, compileStatic) {
                enableReloading = ViewsEnvironment.isDevelopmentMode()
                packageName = Metadata.getCurrent().getApplicationName()
            }
        } 
    }
}
