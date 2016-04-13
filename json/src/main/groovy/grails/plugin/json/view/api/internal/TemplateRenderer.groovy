package grails.plugin.json.view.api.internal

import grails.plugin.json.view.api.GrailsJsonViewHelper
import groovy.transform.CompileStatic

/**
 * Handles the template namespace
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class TemplateRenderer {

    final @Delegate GrailsJsonViewHelper jsonViewHelper

    TemplateRenderer(GrailsJsonViewHelper jsonViewHelper) {
        this.jsonViewHelper = jsonViewHelper
    }

    @Override
    Object invokeMethod(String name, Object args) {
        Object[] argArray = (Object[]) args


        def absolute = name.lastIndexOf('/')
        String modelName = absolute > -1 ? name.substring(absolute+1, name.length()) : name
        int len = argArray.length
        if(len == 1) {
            def val = argArray[0]
            if(val instanceof Map) {
                return jsonViewHelper.render(template:name, model:val)
            }
            else if(val instanceof Iterable) {
                return jsonViewHelper.render(template:name, var:modelName, collection:val)
            }
            else {
                return jsonViewHelper.render(template:name, model:[(modelName): val])
            }
        }
        else if(len == 2) {
            def var = argArray[0]
            def coll = argArray[1]
            if(coll instanceof Iterable) {
                return jsonViewHelper.render(template:name, var:var.toString(), collection:coll)
            }
        }

    }
}
