package grails.plugin.json.view.api.internal

import groovy.transform.CompileStatic

/**
 * @Author Colin Harrington
 */
@CompileStatic
public interface JsonApiIdGenerator {
    String generateId(Object object)
}
