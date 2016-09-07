package grails.plugin.json.view.api.json

import grails.plugin.json.view.api.internal.JsonApiIdGenerator
import groovy.transform.CompileStatic

@CompileStatic
public class DefaultJsonApiIdGenerator implements JsonApiIdGenerator {
    String generateId(Object object) {
        if (object.hasProperty('id')) {
            return object.getAt('id').toString()
        }
    }
}
