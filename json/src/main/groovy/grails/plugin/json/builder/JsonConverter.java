package grails.plugin.json.builder;

import groovy.lang.Closure;

/**
 * An interface for custom converters to implement for service location
 *
 * @author James Kleeh
 * @since 1.2.0
 */
public interface JsonConverter {
    Closure<? extends CharSequence> getConverter();

    Class getType();
}
