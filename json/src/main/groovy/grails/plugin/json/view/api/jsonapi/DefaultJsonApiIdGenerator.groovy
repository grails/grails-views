package grails.plugin.json.view.api.jsonapi

import groovy.transform.CompileStatic

@CompileStatic
public class DefaultJsonApiIdGenerator implements JsonApiIdGenerator {
    String generateId(Object object) {
        if (object && object.hasProperty('id')) {
            return object.getAt('id').toString()
        }
    }
}
