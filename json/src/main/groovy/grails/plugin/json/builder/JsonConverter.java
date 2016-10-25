package grails.plugin.json.builder;

import groovy.lang.Closure;

public interface JsonConverter {
    Closure<? extends CharSequence> getConverter();

    Class getType();
}
