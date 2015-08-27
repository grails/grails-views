package grails.plugins.json.view

import grails.views.ResolvableGroovyTemplateEngine
import grails.views.WritableScript
import grails.views.WritableScriptTemplate
import groovy.text.Template
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

/**
 * A template engine for parsing JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class JsonTemplateEngine extends ResolvableGroovyTemplateEngine {


    /**
     * Constructs a JsonTemplateEngine with the default configuration
     */
    JsonTemplateEngine() {
        this(JsonTemplate.name)
    }

    /**
     * Constructs a JsonTemplateEngine with a custom base class
     *
     * @param baseClassName The name of the base class
     */
    JsonTemplateEngine(String baseClassName, boolean compileStatic = true) {
        super(baseClassName, JsonTemplate.EXTENSION)
        // TODO: Enable this once upgraded to latest Groovy
        if(compileStatic) {

            compilerConfiguration.addCompilationCustomizers(
                    new ASTTransformationCustomizer(Collections.singletonMap("extensions", "grails.plugins.json.view.internal.JsonTemplateTypeCheckingExtension"), CompileStatic.class));
        }
    }


    @Override
    String getDynamicTemplatePrefix() {
        "JsonView".intern()
    }

}
