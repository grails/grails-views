package grails.plugin.json.view

import grails.plugin.json.builder.JsonGenerator
import grails.plugin.json.view.api.jsonapi.JsonApiIdRenderStrategy
import grails.plugin.json.view.internal.JsonTemplateTypeCheckingExtension
import grails.plugin.json.view.internal.JsonViewsTransform
import grails.plugin.json.view.template.JsonViewTemplate
import grails.views.ResolvableGroovyTemplateEngine
import grails.views.ViewConfiguration
import grails.views.WritableScriptTemplate
import grails.views.api.GrailsView
import grails.views.compiler.ViewsTransform
import groovy.text.Template
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.OrderComparator

/**
 * A template engine for parsing JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class JsonViewTemplateEngine extends ResolvableGroovyTemplateEngine {


    private final boolean compileStatic

    final JsonGenerator generator

    @Autowired
    JsonApiIdRenderStrategy jsonApiIdRenderStrategy

    /**
     * Constructs a JsonTemplateEngine with the default configuration
     */
    JsonViewTemplateEngine() {
        this(new JsonViewConfiguration())
    }

    /**
     * Constructs a JsonTemplateEngine with a custom base class
     *
     * @param baseClassName The name of the base class
     */
    JsonViewTemplateEngine(ViewConfiguration configuration) {
        super(configuration)
        this.compileStatic = configuration.compileStatic

        JsonGenerator.Options options = new JsonGenerator.Options()
        JsonViewGeneratorConfiguration config = ((JsonViewConfiguration)configuration).generator

        if (!config.escapeUnicode) {
            options.disableUnicodeEscaping()
        }
        options.dateFormat(config.dateFormat)
        options.timezone(config.timeZone)

        ServiceLoader<JsonGenerator.Converter> loader = ServiceLoader.load(JsonGenerator.Converter.class);
        List<JsonGenerator.Converter> converters = []
        for (JsonGenerator.Converter converter : loader) {
            converters.add(converter)
        }
        OrderComparator.sort(converters)
        converters.each {
            options.addConverter(it)
        }

        this.generator = options.build()
    }

    @Override
    protected void prepareCustomizers(CompilerConfiguration compilerConfiguration) {
        super.prepareCustomizers(compilerConfiguration)
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

    protected WritableScriptTemplate createTemplate(Class<? extends Template> cls, File sourceFile) {
        def template = new JsonViewTemplate((Class<? extends GrailsView>) cls, sourceFile)
        template.generator = this.generator
        template.jsonApiIdRenderStrategy = this.jsonApiIdRenderStrategy
        return initializeTemplate(template, sourceFile)
    }

}
