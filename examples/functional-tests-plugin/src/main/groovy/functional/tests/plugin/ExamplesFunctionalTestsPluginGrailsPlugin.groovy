package functional.tests.plugin

import grails.plugins.*

class ExamplesFunctionalTestsPluginGrailsPlugin extends Plugin {
    def grailsVersion = "6.0.0.BUILD-SNAPSHOT > *"
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]
    def title = "Functional Tests Plugin"
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''
    def profiles = ['web']
    def documentation = "https://grails.org/plugin/examples-functional-tests-plugin"
    def license = "APACHE"
}
