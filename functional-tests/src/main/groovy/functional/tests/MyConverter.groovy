package functional.tests

import grails.plugin.json.builder.JsonConverter
import grails.plugin.json.builder.JsonOutput

/**
 * Created by jameskleeh on 10/25/16.
 */
class MyConverter implements JsonConverter {

    @Override
    Closure<? extends CharSequence> getConverter() {
        { CustomClass c ->
            JsonOutput.toJson(c.name)
        }
    }

    @Override
    Class getType() {
        CustomClass
    }
}
