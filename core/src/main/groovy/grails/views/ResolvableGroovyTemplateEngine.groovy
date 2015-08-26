package grails.views

import grails.util.GrailsStringUtils
import grails.views.compiler.ViewsTransform
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

import java.util.concurrent.ConcurrentHashMap

/**
 * A TemplateEngine that can resolve templates using the configured TemplateResolver
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
abstract class ResolvableGroovyTemplateEngine extends TemplateEngine {

    private static final Template NULL_ENTRY = new Template() {
        @Override
        Writable make() {}
        @Override
        Writable make(Map binding) {}
    }

    private Map<String, Template> cachedTemplates = new ConcurrentHashMap<String, Template>()
            .withDefault { String path ->
        def cls = templateResolver.resolveTemplateClass(packageName, path)
        if(cls != null) {
            return createTemplate( (Class<? extends Template>)cls )
        }
        else {
            def url = templateResolver.resolveTemplate(path)
            if(url != null) {
                return createTemplate(path, url)
            }
        }
        return NULL_ENTRY
    }

    private int templateCounter

    /**
     * The TemplateResolver to use
     */
    TemplateResolver templateResolver  = new GenericGroovyTemplateResolver()

    /**
     * The package name that contains the template
     */
    String packageName = ""

    /**
     * The class loader to use
     */
    GroovyClassLoader classLoader
    /**
     * The configuration to use for compilation
     */
    CompilerConfiguration compilerConfiguration

    /**
     * The view uri resolver
     */
    final ViewUriResolver viewUriResolver

    private ASTTransformationCustomizer currentCustomizer
    /**
     * Creates a ResolvableGroovyTemplateEngine for the given base class name and file extension
     *
     * @param baseClassName The base class name
     * @param extension The file extension
     */
    ResolvableGroovyTemplateEngine(String baseClassName, String extension) {
        this.compilerConfiguration = new CompilerConfiguration()
        this.viewUriResolver = new GenericViewUriResolver(".$extension")
        compilerConfiguration.setScriptBaseClass(baseClassName)
        this.currentCustomizer = new ASTTransformationCustomizer(new ViewsTransform())
        compilerConfiguration.addCompilationCustomizers( currentCustomizer )
        classLoader = new GroovyClassLoader(Thread.currentThread().contextClassLoader, compilerConfiguration)
    }

    /**
     * Creates a template for the given template class
     *
     * @param cls The class
     * @return The template
     */
    protected abstract Template createTemplate(Class<? extends Template> cls)

    /**
     * Resolves a template for the given path
     * @param path The path to the template
     * @return The template or null if it doesn't exist
     */
    Template resolveTemplate(String path) {
        def template = cachedTemplates[path]
        if(template.is(NULL_ENTRY)) {
            return null
        }
        return template
    }

    @Override
    Template createTemplate(File file) throws CompilationFailedException, ClassNotFoundException, IOException {
        def cls = classLoader.parseClass(file)
        return createTemplate(cls)
    }

    @Override
    Template createTemplate(URL url) throws CompilationFailedException, ClassNotFoundException, IOException {
        def file = url.file
        def basename = GrailsStringUtils.getFileBasename(file)
        createTemplate("/$basename", url)
    }

    Template createTemplate(String path, URL url) throws CompilationFailedException, ClassNotFoundException, IOException {
        // Had to do this hack because of a Groovy bug where ASTTransformationCustomizer are only applied once!?
        compilerConfiguration.compilationCustomizers.remove(currentCustomizer)
        currentCustomizer = new ASTTransformationCustomizer(new ViewsTransform())
        compilerConfiguration.compilationCustomizers.add(currentCustomizer)

        // now parse the class
        url.withReader { Reader reader ->
            def viewScriptName = GenericGroovyTemplateResolver.resolveTemplateName(packageName, path)
            def clazz = classLoader.parseClass(new GroovyCodeSource(reader, viewScriptName, GroovyShell.DEFAULT_CODE_BASE))
            return createTemplate(clazz)
        }
    }

    @Override
    Template createTemplate(Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {

        // if we reach here, use a throw away child class loader for dynamic templates
        def clazz = new GroovyClassLoader(classLoader).parseClass(new GroovyCodeSource(reader, getDynamicTemplatePrefix() + templateCounter++, GroovyShell.DEFAULT_CODE_BASE))
        return createTemplate(clazz)
    }

    abstract String getDynamicTemplatePrefix()
}