package grails.plugin.json.view

import grails.plugin.json.view.api.GrailsJsonViewHelper
import grails.plugin.json.view.api.internal.TemplateRenderer
import spock.lang.Specification

/**
 * Created by graemerocher on 13/04/16.
 */
class TemplateRendererSpec extends Specification {

    void "Test template renderer calls the correct render method"() {
        given:"A template renderer"

        def mockViewHelper = Mock(GrailsJsonViewHelper)
        def renderer = new TemplateRenderer(mockViewHelper)

        def o = new Object()
        when:
        renderer.foo(o)

        then:
        1 * mockViewHelper.render([template:"foo", model:[foo:o]])

        when:
        renderer.foo([o])

        then:
        1 * mockViewHelper.render([template:"foo", var:'foo', collection:[o]])

        when:
        renderer."/foo/foo"([o])

        then:
        1 * mockViewHelper.render([template:"/foo/foo", var:'foo', collection:[o]])
    }
}
