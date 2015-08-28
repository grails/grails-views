package grails.plugin.json.view

import grails.views.GenericViewUriResolver
import spock.lang.Specification

/**
 * Created by graemerocher on 25/08/15.
 */
class GenericViewUriResolverSpec extends Specification{

    void "test resolve template URIs"() {
        given:
            def resolver = new GenericViewUriResolver(".gson")

        expect:
            resolver.resolveTemplateUri("foo", "bar") == '/foo/_bar.gson'
    }
}
