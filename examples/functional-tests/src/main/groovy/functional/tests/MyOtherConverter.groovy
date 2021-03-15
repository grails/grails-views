package functional.tests

import grails.plugin.json.builder.JsonGenerator
import org.springframework.core.Ordered

/**
 * Created by jameskleeh on 12/12/16.
 */
class MyOtherConverter implements JsonGenerator.Converter, Ordered {

    @Override
    boolean handles(Class<?> type) {
        CustomClass.isAssignableFrom(type)
    }

    @Override
    Object convert(Object value, String key) {
        "shouldn't see this value because MyConverter has a higher priority of -2"
    }

    @Override
    int getOrder() {
        return -1
    }
}
