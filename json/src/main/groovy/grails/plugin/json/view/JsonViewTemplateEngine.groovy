package grails.plugin.json.view

import grails.plugin.json.view.internal.JsonTemplateTypeCheckingExtension
import grails.views.ResolvableGroovyTemplateEngine
import grails.views.ViewConfiguration
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

/**
 * A template engine for parsing JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class JsonViewTemplateEngine extends ResolvableGroovyTemplateEngine {
    public static final String VIEW_BASE_CLASS = 'grails.views.json.baseClass'
    public static final String COMPILE_STATIC = 'grails.views.json.compileStatic'


    private final boolean compileStatic
    /**
     * Constructs a JsonTemplateEngine with the default configuration
     */
    JsonViewTemplateEngine() {
        this(new JsonViewConfiguration(baseTemplateClass: JsonViewTemplate))
    }

    /**
     * Constructs a JsonTemplateEngine with a custom base class
     *
     * @param baseClassName The name of the base class
     */
    JsonViewTemplateEngine(ViewConfiguration configuration) {
        super(configuration)
        this.compileStatic = configuration.compileStatic
    }

    @Override
    protected void prepareCustomizers() {
        super.prepareCustomizers()
        if(compileStatic) {
            compilerConfiguration.addCompilationCustomizers(
                    new ASTTransformationCustomizer(Collections.singletonMap("extensions", JsonTemplateTypeCheckingExtension.name), CompileStatic.class));
        }
    }

    @Override
    String getDynamicTemplatePrefix() {
        "JsonView".intern()
    }

}
