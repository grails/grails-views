package grails.plugin.markup.view

import grails.views.ResolvableGroovyTemplateEngine
import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import groovy.text.markup.TemplateResolver
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.CompilationCustomizer

/**
 * A {@link ResolvableGroovyTemplateEngine} that uses Groovy's {@link MarkupTemplateEngine} internally
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class MarkupViewTemplateEngine extends ResolvableGroovyTemplateEngine {

    public static final String VIEW_BASE_CLASS = 'grails.views.markup.baseClass'
    public static final String COMPILE_STATIC = 'grails.views.markup.compileStatic'


    MarkupTemplateEngine innerEngine

    private final boolean compileStatic

    MarkupViewTemplateEngine(MarkupViewConfiguration config = new MarkupViewConfiguration()) {
        super(config)
        this.compileStatic = compileStatic

        innerEngine = new MarkupTemplateEngine(Thread.currentThread().contextClassLoader, config, new TemplateResolver() {
            @Override
            void configure(ClassLoader templateClassLoader, TemplateConfiguration configuration) {
            }

            @Override
            URL resolveTemplate(String templatePath) throws IOException {
                return templateResolver.resolveTemplate(templatePath)
            }
        })
        prepareCustomizers()
        innerEngine.compilerConfiguration.addCompilationCustomizers( compilerConfiguration.compilationCustomizers as CompilationCustomizer[])

    }

    @Override
    protected void prepareCustomizers() {
        super.prepareCustomizers()

        if(compileStatic) {
            compilerConfiguration.addCompilationCustomizers(
                    new ASTTransformationCustomizer(Collections.singletonMap("extensions", "groovy.text.markup.MarkupTemplateTypeCheckingExtension"), CompileStatic.class));
        }
    }

    @Override
    Template createTemplate(String path, URL url) throws CompilationFailedException, ClassNotFoundException, IOException {
        return innerEngine.createTemplate(url)
    }

    @Override
    Template createTemplate(File file) throws CompilationFailedException, ClassNotFoundException, IOException {
        return innerEngine.createTemplate(file.toURI().toURL())
    }

    @Override
    Template createTemplate(Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        return innerEngine.createTemplate(reader)
    }

    @Override
    String getDynamicTemplatePrefix() {
        "GeneratedMarkupTemplate".intern()
    }
}
