package grails.views.resolve

import javassist.util.proxy.ProxyFactory
import spock.lang.Specification

/**
 * Created by graemerocher on 19/05/16.
 */
class TemplateResolveUtilsSpec extends Specification {

    void "Test template name calculations"() {
        expect:
        TemplateResolverUtils.shortTemplateNameForClass(Object) == '/object/_object'
        TemplateResolverUtils.fullTemplateNameForClass(Object) == '/java/lang/_object'
    }

    void "Test template name calculations for javassist proxy"() {
        when:
        ProxyFactory f = new ProxyFactory()
        f.setSuperclass(TemplateResolveModel)
        def test = f.createClass()
        then:
        TemplateResolverUtils.shortTemplateNameForClass(test) == '/templateResolveModel/_templateResolveModel'
        TemplateResolverUtils.fullTemplateNameForClass(test) == '/grails/views/resolve/_templateResolveModel'
    }
}
