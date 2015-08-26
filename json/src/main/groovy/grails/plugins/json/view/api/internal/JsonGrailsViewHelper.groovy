package grails.plugins.json.view.api.internal

import grails.plugins.json.builder.JsonOutput
import grails.plugins.json.view.api.GrailsJsonViewHelper
import grails.views.ViewException
import grails.views.api.internal.DefaultGrailsViewHelper
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.buffer.FastStringWriter

/**
 * Extended version of {@link DefaultGrailsViewHelper} with methods specific to JSON view rendering
 *
 * @author Graeme Rocher
 */
@CompileStatic
@InheritConstructors
class JsonGrailsViewHelper extends DefaultGrailsViewHelper implements GrailsJsonViewHelper {

    @Override
    JsonOutput.JsonUnescaped render(Map arguments) {
        def template = arguments.template

        def templateEngine = view.templateEngine
        if(template) {
            Map model = (Map)arguments.model ?: [:]
            def collection = arguments.collection
            def var = arguments.var ?: 'it'
            def templateUri = templateEngine
                    .viewUriResolver
                    .resolveTemplateUri(view.getControllerName(), template.toString())
            def childTemplate = templateEngine.resolveTemplate(templateUri)
            if(childTemplate != null) {
                FastStringWriter stringWriter = new FastStringWriter()
                if(collection instanceof Iterable) {
                    Iterable iterable = (Iterable)collection
                    int size = iterable.size()
                    int i = 0
                    stringWriter << '['
                    for(o in collection) {
                        model.put(var, o)
                        def writable = childTemplate.make(model)
                        writable.writeTo( stringWriter )
                        if(++i != size) {
                            stringWriter << ','
                        }
                    }
                    stringWriter << ']'
                }
                else {
                    def writable = model ? childTemplate.make((Map)model) : childTemplate.make()
                    writable.writeTo( stringWriter )
                }

                return JsonOutput.unescaped( stringWriter.toString() )
            }
            else {
                throw new ViewException("Template not found for name $template")
            }
        }

    }
}
