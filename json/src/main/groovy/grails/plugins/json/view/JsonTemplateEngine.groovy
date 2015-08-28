package grails.plugins.json.view

import grails.views.ResolvableGroovyTemplateEngine
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
    public static final String VIEW_BASE_CLASS = 'grails.views.json.baseClass'
    public static final String COMPILE_STATIC = 'grails.views.json.compileStatic'


    private final boolean compileStatic
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
        this.compileStatic = compileStatic
        prepareCustomizers()
    }

    @Override
    protected void prepareCustomizers() {
        super.prepareCustomizers()
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
