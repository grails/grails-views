package grails.plugin.json.view.api.jsonapi

import groovy.transform.CompileStatic

@CompileStatic
public class DefaultJsonApiIdGenerator implements JsonApiIdGenerator {
    String generateId(Object object) {
        generateId(object, 'id')
    }

    String generateId(Object object, String propertyName) {
        if (object && object.hasProperty(propertyName)) {
            return object.getAt(propertyName).toString()
        }
    }
}
