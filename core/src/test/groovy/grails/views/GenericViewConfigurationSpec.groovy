package grails.views

import grails.util.GrailsUtil
import org.grails.config.CodeGenConfig
import spock.lang.IgnoreIf
import spock.lang.Specification

//Fixed in 3.2.5
@IgnoreIf( { GrailsUtil.grailsVersion.startsWith('3.1') })
class GenericViewConfigurationSpec extends Specification {

    void "test setting of boolean"() {
        given:
        def testClass = new TestClass()
        String yml = 'grails.views.json.compileStatic: false'
        CodeGenConfig config = new CodeGenConfig()
        config.loadYml(new ByteArrayInputStream(yml.bytes))

        expect:
        testClass.compileStatic
        !testClass.useAbsoluteLinks

        when: "no relevant properties in the config"
        testClass.readConfiguration(config)

        then: "the properties don't change from the defaults"
        !testClass.compileStatic
        !testClass.useAbsoluteLinks
    }
}

class TestClass implements GenericViewConfiguration {
    @Override
    String getViewModuleName() {
        "json"
    }
}
