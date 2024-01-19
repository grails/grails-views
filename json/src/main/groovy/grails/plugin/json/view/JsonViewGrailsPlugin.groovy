/*
 * Copyright 2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.json.view

import grails.plugin.json.view.api.jsonapi.DefaultJsonApiIdRenderer
import grails.plugin.json.view.mvc.JsonViewResolver
import grails.plugins.Plugin
import grails.views.mvc.GenericGroovyTemplateViewResolver
import grails.views.resolve.PluginAwareTemplateResolver

class JsonViewGrailsPlugin extends Plugin {

    def grailsVersion = "6.1.0 > *"

    def title = "JSON View"
    def author = "Puneet Behl"
    def authorEmail = "behlp@unityfoundation.io"
    def description = '''\
A plugin that allows rendering of JSON views
'''
    def profiles = ['web']
    def documentation = "https://grails.github.io/grails-views/latest"
    def license = "APACHE"
    def organization = [ name: "Unity Foundation", url: "https://unityfoundation.io/" ]
    def developers = [ [ name: "Puneet Behl", email: "behlp@unityfoundation.io" ]]
    def issueManagement = [ system: "Github", url: "https://github.com/grails/grails-views/issues" ]
    def scm = [ url: "https://github.com/grails/grails-views" ]

    Closure doWithSpring() { {->
            jsonApiIdRenderStrategy(DefaultJsonApiIdRenderer)
            jsonViewConfiguration(JsonViewConfiguration)
            jsonTemplateEngine(JsonViewTemplateEngine, jsonViewConfiguration, applicationContext.classLoader)
            jsonSmartViewResolver(JsonViewResolver, jsonTemplateEngine) {
                templateResolver = bean(PluginAwareTemplateResolver, jsonViewConfiguration)
            }
            jsonViewResolver(GenericGroovyTemplateViewResolver, jsonSmartViewResolver )
        }
    }
}
