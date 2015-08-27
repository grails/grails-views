package grails.views

import grails.util.GrailsStringUtils
import grails.views.compiler.ViewsTransform
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.grails.io.watch.DirectoryWatcher

import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap

/**
 * A TemplateEngine that can resolve templates using the configured TemplateResolver
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
abstract class ResolvableGroovyTemplateEngine extends TemplateEngine implements Closeable {

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
            if(url == null) {
                url = templateResolver.resolveTemplate("${path}.${extension}")
            }
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
    final GroovyClassLoader classLoader
    /**
     * The configuration to use for compilation
     */
    final CompilerConfiguration compilerConfiguration

    /**
     * The view uri resolver
     */
    final ViewUriResolver viewUriResolver

    /**
     * The file extension used for script templates
     */
    final String extension

    /**
     * Whether to reload views
     */
    boolean enableReloading = false

    /**
     * Used to watch for file changes
     */
    private DirectoryWatcher directoryWatcher

    private Map<String, String> watchedFilePaths = new ConcurrentHashMap<String, String>()

    private ASTTransformationCustomizer currentCustomizer

    /**
     * Creates a ResolvableGroovyTemplateEngine for the given base class name and file extension
     *
     * @param baseClassName The base class name
     * @param extension The file extension
     */
    ResolvableGroovyTemplateEngine(String baseClassName, String extension) {
        this.extension = extension
        this.compilerConfiguration = new CompilerConfiguration()
        this.viewUriResolver = new GenericViewUriResolver(".$extension")
        compilerConfiguration.setScriptBaseClass(baseClassName)
        this.currentCustomizer = new ASTTransformationCustomizer(new ViewsTransform())
        compilerConfiguration.addCompilationCustomizers( this.currentCustomizer )
        classLoader = new GroovyClassLoader(Thread.currentThread().contextClassLoader, compilerConfiguration)
    }

    void setEnableReloading(boolean enableReloading) {
        this.enableReloading = enableReloading
        this.directoryWatcher = new DirectoryWatcher()
        this.directoryWatcher.addListener(new DirectoryWatcher.FileChangeListener() {
            @Override
            void onChange(File file) {
                def path = watchedFilePaths[file.canonicalPath]
                if(path != null) {
                    cachedTemplates.remove(path)
                    cachedTemplates.remove("${path}.${extension}".toString())
                }
            }

            @Override
            void onNew(File file) {
                onChange(file)
            }
        })
        this.directoryWatcher.start()
    }

    @Override
    @PreDestroy
    void close() throws IOException {
        if(directoryWatcher != null) {
            try {
                directoryWatcher.setActive(false)
            } catch (Throwable e) {
                // ignore
            }
        }
    }

    /**
     * Creates a template for the given template class
     *
     * @param cls The class
     * @return The template
     */
    protected Template createTemplate(Class<? extends Template> cls) {
        createTemplate(cls, null)
    }

    /**
     * Creates a template for the given template class
     *
     * @param cls The class
     * @return The template
     */
    protected Template createTemplate(Class<? extends Template> cls, File sourceFile) {
        def template = new WritableScriptTemplate((Class<? extends WritableScript>) cls)
        template.setSourceFile(sourceFile)
        return template
    }

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
        return createTemplate(cls, file)
    }

    @Override
    Template createTemplate(URL url) throws CompilationFailedException, ClassNotFoundException, IOException {
        def file = url.file
        def basename = GrailsStringUtils.getFileBasename(file)
        createTemplate("/$basename", url)
    }

    Template createTemplate(String path, URL url) throws CompilationFailedException, ClassNotFoundException, IOException {
        // Had to do this hack because of a Groovy bug where ASTTransformationCustomizer are only applied once!?
        def file = new File(url.file)
        if(directoryWatcher != null) {
            def pathToFile = file.canonicalPath
            if(!watchedFilePaths.containsKey(pathToFile)) {
                watchedFilePaths.put(pathToFile, path)
                directoryWatcher.addWatchFile(
                        file
                )
            }
        }


        // this hack is required because of https://issues.apache.org/jira/browse/GROOVY-7560
        compilerConfiguration.compilationCustomizers.remove(currentCustomizer)
        compilerConfiguration.compilationCustomizers.add(new ASTTransformationCustomizer(new ViewsTransform()))
        def classLoader = new GroovyClassLoader(classLoader, compilerConfiguration)
        // now parse the class
        url.withReader { Reader reader ->
            def viewScriptName = GenericGroovyTemplateResolver.resolveTemplateName(packageName, path)
            try {
                def clazz = classLoader.parseClass(new GroovyCodeSource(reader, viewScriptName, GroovyShell.DEFAULT_CODE_BASE))
                return createTemplate(clazz, file)
            } catch (CompilationFailedException e) {
                throw new ViewCompilationException(e, file.canonicalPath)
            }
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