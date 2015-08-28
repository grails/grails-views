package grails.views

import grails.util.GrailsStringUtils
import grails.views.compiler.ViewsTransform
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
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
    final ViewUriResolver viewUriResolver

    /**
     * The view uri resolver
     */
    final String extension

    /**
     * The file extension used for script templates
     */
    boolean enableReloading = false

    /**
     * Whether to reload views
     */
    protected CompilerConfiguration compilerConfiguration

    /**
     * Used to watch for file changes
     */
    private DirectoryWatcher directoryWatcher

    private Map<String, String> watchedFilePaths = new ConcurrentHashMap<String, String>()


    /**
     * Creates a ResolvableGroovyTemplateEngine for the given base class name and file extension
     *
     * @param baseClassName The base class name
     * @param extension The file extension
     */
    ResolvableGroovyTemplateEngine(TemplateConfiguration configuration) {
        this.extension = configuration.extension
        setPackageName(configuration.packageName)
        setEnableReloading(configuration.enableReloading)
        this.compilerConfiguration = new CompilerConfiguration()
        this.viewUriResolver = new GenericViewUriResolver(".$extension")
        compilerConfiguration.setScriptBaseClass(configuration.baseTemplateClass.name)
        classLoader = new GroovyClassLoader(Thread.currentThread().contextClassLoader, compilerConfiguration)
    }

    CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration
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
        resolveTemplate(path, Locale.ENGLISH)
    }

    /**
     * Resolves a template for the given path
     * @param path The path to the template
     * @param locale The locale of the template
     *
     * @return The template or null if it doesn't exist
     */
    Template resolveTemplate(String path, Locale locale) {
        String extensionSuffix = ".$extension"
        String originalPath = path - extensionSuffix
        String defaultPath = "${originalPath}${extensionSuffix}"
        String localeSpecificPath = locale ? "${originalPath}_${locale}${extensionSuffix}" : defaultPath
        String languageSpecificPath = locale ? "${originalPath}_${locale.language}${extensionSuffix}" : defaultPath

        Template template = cachedTemplates[localeSpecificPath]
        if(template.is(NULL_ENTRY)) {
            template = cachedTemplates[languageSpecificPath]
        }
        else {
            return template
        }
        if(template.is(NULL_ENTRY)) {
            template = cachedTemplates[defaultPath]
        }
        else {
            return template
        }

        if(template.is(NULL_ENTRY)) {
            return null
        }
        else {
            cachedTemplates.put(localeSpecificPath, template)
            cachedTemplates.put(languageSpecificPath, template)
            return template
        }
    }

    @Override
    Template createTemplate(File file) throws CompilationFailedException, ClassNotFoundException, IOException {
        prepareCustomizers()
        def classLoader = new GroovyClassLoader(classLoader, compilerConfiguration)
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
        watchIfNecessary(file, path)
        prepareCustomizers()
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

    protected void watchIfNecessary(File file, String path) {
        if (directoryWatcher != null) {
            def pathToFile = file.canonicalPath
            if (!watchedFilePaths.containsKey(pathToFile)) {
                watchedFilePaths.put(pathToFile, path)
                directoryWatcher.addWatchFile(
                        file
                )
            }
        }
    }

    @Override
    Template createTemplate(Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        prepareCustomizers()
        // if we reach here, use a throw away child class loader for dynamic templates
        def fileName = getDynamicTemplatePrefix() + templateCounter++
        try {
            def clazz = new GroovyClassLoader(classLoader).parseClass(new GroovyCodeSource(reader, fileName, GroovyShell.DEFAULT_CODE_BASE))
            return createTemplate(clazz)
        } catch (CompilationFailedException e) {
            throw new ViewCompilationException(e, fileName)
        }
    }

    abstract String getDynamicTemplatePrefix()

    protected void prepareCustomizers() {
        // this hack is required because of https://issues.apache.org/jira/browse/GROOVY-7560
        compilerConfiguration.compilationCustomizers.clear()
        compilerConfiguration.compilationCustomizers.add(new ASTTransformationCustomizer(new ViewsTransform()))
    }
}