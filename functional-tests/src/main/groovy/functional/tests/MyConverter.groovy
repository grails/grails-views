package functional.tests

import grails.plugin.json.builder.JsonGenerator

/**
 * Created by jameskleeh on 10/25/16.
 */
class MyConverter implements JsonGenerator.Converter {

    @Override
    boolean handles(Class<?> type) {
        CustomClass.isAssignableFrom(type)
    }

    @Override
    Object convert(Object value, String key) {
        ((CustomClass)value).name
    }
}
