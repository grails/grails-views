package grails.plugin.json.view

import grails.compiler.traits.TraitInjector
import grails.plugin.json.view.api.JsonView
import grails.plugin.json.view.internal.JsonTemplateTypeCheckingExtension
import grails.plugin.json.view.internal.JsonViewsTransform
import grails.views.ResolvableGroovyTemplateEngine
import grails.views.ViewConfiguration
import grails.views.compiler.ViewsTransform
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.grails.core.io.support.GrailsFactoriesLoader

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
    protected ViewsTransform newViewsTransform() {
        return new JsonViewsTransform(viewConfiguration.extension)
    }

    @Override
    String getDynamicTemplatePrefix() {
        "JsonView".intern()
    }

}
