package grails.plugin.json.view.api.jsonapi

import groovy.transform.CompileStatic

/**
 * @Author Colin Harrington
 */
@CompileStatic
public interface JsonApiIdGenerator {

    String generateId(Object object)

    String generateId(Object object, String propertyName)
}
