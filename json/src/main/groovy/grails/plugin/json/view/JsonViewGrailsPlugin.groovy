package grails.plugin.json.view

import grails.plugin.json.renderer.ErrorsJsonViewRenderer
import grails.plugin.json.view.api.jsonapi.DefaultJsonApiIdRenderer
import grails.plugin.json.view.api.jsonapi.JsonApiIdRenderStrategy
import grails.plugin.json.view.mvc.JsonViewResolver
import grails.plugins.*
import grails.util.BuildSettings
import grails.util.Environment
import grails.util.Metadata
import grails.views.ViewsEnvironment
import grails.views.mvc.GenericGroovyTemplateViewResolver
import grails.views.resolve.PluginAwareTemplateResolver
import org.grails.io.support.GrailsResourceUtils
import org.springframework.validation.Errors

class JsonViewGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "4.0.0.BUILD-SNAPSHOT > *"

    def title = "JSON View" // Headline display name of the plugin
    def author = "Graeme Rocher"
    def authorEmail = "graeme.rocher@gmail.com"
    def description = '''\
A plugin that allows rendering of JSON views
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.github.io/grails-views/latest"

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
            jsonApiIdRenderStrategy(DefaultJsonApiIdRenderer)
            jsonViewConfiguration(JsonViewConfiguration)
            jsonTemplateEngine(JsonViewTemplateEngine, jsonViewConfiguration)
            jsonSmartViewResolver(JsonViewResolver, jsonTemplateEngine) {
                templateResolver = bean(PluginAwareTemplateResolver, jsonViewConfiguration)
            }
            jsonViewResolver(GenericGroovyTemplateViewResolver, jsonSmartViewResolver )
        }
    }
}
