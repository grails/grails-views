package grails.views

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import grails.core.support.proxy.DefaultProxyHandler
import grails.core.support.proxy.ProxyHandler
import grails.util.GrailsStringUtils
import grails.views.api.GrailsView
import grails.views.compiler.ViewsTransform
import grails.views.resolve.GenericGroovyTemplateResolver
import grails.views.resolve.GenericViewUriResolver
import grails.views.resolve.TemplateResolverUtils
import grails.web.mapping.LinkGenerator
import grails.web.mime.MimeUtility
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.grails.datastore.mapping.model.MappingContext
import org.grails.web.mime.DefaultMimeUtility
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.context.support.StaticMessageSource

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

    protected Map<List, Template> resolveCache = new ConcurrentLinkedHashMap.Builder<List, Template>()
                                                                            .maximumWeightedCapacity(250)
                                                                            .build()

    protected Map<String, Template> cachedTemplates = new ConcurrentLinkedHashMap.Builder<String, Template>()
            .maximumWeightedCapacity(250)
            .build()
            .withDefault { String path ->
        def cls = templateResolver.resolveTemplateClass(path)
        if(cls != null) {
            return createTemplate( (Class<? extends Template>)cls )
        }
        else {
            def url = templateResolver.resolveTemplate(path)
            if(url == null && !path.endsWith(extension)) {
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
    TemplateResolver templateResolver

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
     * Whether to enable reloading
     */
    final boolean enableReloading

    final boolean shouldCache

    /**
     * Whether to reload views
     */
    protected CompilerConfiguration compilerConfiguration

    /**
     * The view config
     */
    @Delegate final ViewConfiguration viewConfiguration

    private MessageSource messageSource = new StaticMessageSource()

    private MimeUtility mimeUtility = new DefaultMimeUtility()

    private ProxyHandler proxyHandler = new DefaultProxyHandler()

    private LinkGenerator linkGenerator

    private MappingContext mappingContext

    /**
     * Creates a ResolvableGroovyTemplateEngine for the given base class name and file extension
     *
     * @param baseClassName The base class name
     * @param extension The file extension
     */
    ResolvableGroovyTemplateEngine(ViewConfiguration configuration) {
        this.viewConfiguration = configuration
        this.enableReloading = configuration.enableReloading
        this.shouldCache = configuration.cache
        this.templateResolver = new GenericGroovyTemplateResolver(packageName: configuration.packageName, baseDir: new File(configuration.templatePath))
        this.extension = configuration.extension
        this.compilerConfiguration = new CompilerConfiguration()
        this.viewUriResolver = new GenericViewUriResolver(".$extension")
        compilerConfiguration.setScriptBaseClass(configuration.baseTemplateClass.name)
        prepareCustomizers(compilerConfiguration)
        classLoader = new GroovyClassLoader(getClass().getClassLoader(), new CompilerConfiguration(compilerConfiguration))
    }

    @Autowired(required = false)
    void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource
    }

    @Autowired(required = false)
    void setMimeUtility(MimeUtility mimeUtility) {
        this.mimeUtility = mimeUtility
    }

    @Autowired(required = false)
    void setProxyHandler(ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler
    }

    @Autowired
    void setLinkGenerator(LinkGenerator linkGenerator) {
        this.linkGenerator = linkGenerator
    }

    @Autowired(required = false)
    @Qualifier("grailsDomainClassMappingContext")
    void setMappingContext(MappingContext mappingContext) {
        this.mappingContext = mappingContext
    }

    CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration
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
        def template = new GrailsViewTemplate((Class<? extends GrailsView>) cls, sourceFile)
        return initializeTemplate(template, sourceFile)
    }

    /**
     * Initialises a template instance
     *
     * @param template The created template
     * @param sourceFile The source file
     * @return The initialized template
     */
    protected GrailsViewTemplate initializeTemplate(GrailsViewTemplate template, File sourceFile) {
        template.setSourceFile(sourceFile)
        template.setPrettyPrint(viewConfiguration.prettyPrint)
        template.setMessageSource(messageSource)
        template.setMimeUtility(mimeUtility)
        template.setLinkGenerator(linkGenerator)
        template.setMappingContext(mappingContext)
        template.setTemplateEngine(this)
        return template
    }

    /**
     * Resolves a template for the given object
     *
     * @param object The object
     * @param qualifiers One or many qualifiers to scope the view (for example the locale, the version etc.)
     *
     * @return The template or null if it doesn't exist
     */
    Template resolveTemplate(Class type, Locale locale, String...qualifiers) {
        Template t = resolveTemplate(TemplateResolverUtils.fullTemplateNameForClass(type), locale, qualifiers)
        if(t == null) {
            t = resolveTemplate(TemplateResolverUtils.shortTemplateNameForClass(type), locale, qualifiers)
        }
        return t
    }

    /**
     * Resolves a template for the given path
     * @param path The path to the template
     *
     * @return The template or null if it doesn't exist
     */
    Template resolveTemplate(String path) {
        resolveTemplate(path, Locale.ENGLISH)
    }
    /**
     * Resolves a template for the given path
     * @param path The path to the template
     * @param qualifiers One or many qualifiers to scope the view (for example the locale, the version etc.)
     *
     * @return The template or null if it doesn't exist
     */
    Template resolveTemplate(String path, Locale locale, String...qualifiers) {
        if(locale == null) {
            locale = Locale.ENGLISH
        }
        def cacheKey = [path, locale.language]
        cacheKey.addAll(qualifiers)
        Template template = null
        if(shouldCache) {
            template = resolveCache.get(cacheKey)
            if(template != null) {
                if(template.is(NULL_ENTRY)) {
                    return null
                }
                else if( !enableReloading || !((GrailsViewTemplate)template).wasModified()) {
                    return template
                }
                else {
                    cachedTemplates.remove(path)
                    resolveCache.remove(cacheKey)
                    template = null
                }
            }
        }



        String extensionSuffix = ".$extension"
        String originalPath = path - extensionSuffix
        String defaultPath = "${originalPath}${extensionSuffix}"
        String language = locale.language
        String defaultLanguageSpecificPath = "${originalPath}_${language}${extensionSuffix}"

        List<String> qualifiedPaths = [defaultPath, defaultLanguageSpecificPath]
        if(qualifiers) {
            Queue<String> qualifierQueue = new ArrayDeque<String>()
            qualifierQueue.addAll(qualifiers)

            while(qualifierQueue.peekLast() != null) {
                boolean isEmpty = qualifierQueue.isEmpty()
                String qualified = !isEmpty ? "_${qualifierQueue.join('_')}" : ""
                String qualifiedLanguageSpecificPath = "${originalPath}_${language}${qualified}${extensionSuffix}"
                String qualifiedPath = "${originalPath}${qualified}${extensionSuffix}"
                qualifiedPaths.add qualifiedPath
                qualifiedPaths.add qualifiedLanguageSpecificPath

                template = cachedTemplates[qualifiedLanguageSpecificPath]
                if(template.is(NULL_ENTRY)) {
                    template = cachedTemplates[qualifiedPath]
                    if(template.is(NULL_ENTRY) && !isEmpty) {
                        qualifierQueue.removeLast()
                    }
                    else {
                        break
                    }
                }
                else {
                    break
                }
            }

            if(template == null || template.is(NULL_ENTRY)) {
                qualifierQueue.addAll(qualifiers.reverse())
                while(qualifierQueue.peekLast() != null) {
                    boolean isEmpty = qualifierQueue.isEmpty()
                    String qualified = !isEmpty ? "_${qualifierQueue.join('_')}" : ""
                    String qualifiedLanguageSpecificPath = "${originalPath}_${language}${qualified}${extensionSuffix}"
                    String qualifiedPath = "${originalPath}${qualified}${extensionSuffix}"
                    qualifiedPaths.add qualifiedPath
                    qualifiedPaths.add qualifiedLanguageSpecificPath

                    template = cachedTemplates[qualifiedLanguageSpecificPath]
                    if(template.is(NULL_ENTRY)) {
                        template = cachedTemplates[qualifiedPath]
                        if(template.is(NULL_ENTRY) && !isEmpty) {
                            qualifierQueue.removeLast()
                        }
                        else {
                            break
                        }
                    }
                    else {
                        break
                    }
                }
            }
        }
        if(template == null || template.is(NULL_ENTRY)) {
            template = cachedTemplates[defaultLanguageSpecificPath]
            if(template.is(NULL_ENTRY)) {
                template = cachedTemplates[defaultPath]
            }
        }
        if(template != null) {

            boolean isNull = template.is(NULL_ENTRY)
            if(!isNull && ((GrailsViewTemplate)template).wasModified()) {
                for(p in qualifiedPaths) {
                    cachedTemplates.remove(p)
                    resolveCache.remove(cacheKey)
                }
                return resolveTemplate(path, locale, qualifiers)
            }
            else {
                if(shouldCache) {
                    resolveCache.put(cacheKey, template)
                }
                if(isNull) {
                    return null
                }
                else {
                    return template
                }
            }

        }
        return template
    }

    @Override
    Template createTemplate(File file) throws CompilationFailedException, ClassNotFoundException, IOException {
        def cc = new CompilerConfiguration(compilerConfiguration)
        prepareCustomizers(cc)

        def classLoader = new GroovyClassLoader(classLoader, cc)
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
        def cc = new CompilerConfiguration(compilerConfiguration)
        prepareCustomizers(cc)

        def classLoader = new GroovyClassLoader(classLoader, cc)
        // now parse the class
        url.withReader { Reader reader ->
            def viewScriptName = GenericGroovyTemplateResolver.resolveTemplateName(viewConfiguration.packageName, path)
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
        def cc = new CompilerConfiguration(compilerConfiguration)
        prepareCustomizers(cc)
        // if we reach here, use a throw away child class loader for dynamic templates
        def fileName = getDynamicTemplatePrefix() + templateCounter++
        try {
            def clazz = new GroovyClassLoader(classLoader, cc).parseClass(new GroovyCodeSource(reader, fileName, GroovyShell.DEFAULT_CODE_BASE))
            return createTemplate(clazz)
        } catch (CompilationFailedException e) {
            throw new ViewCompilationException(e, fileName)
        }
    }

    abstract String getDynamicTemplatePrefix()

    protected void prepareCustomizers(CompilerConfiguration compilerConfiguration) {
        // this hack is required because of https://issues.apache.org/jira/browse/GROOVY-7560
        compilerConfiguration.compilationCustomizers.clear()


        def importCustomizer = new ImportCustomizer()
        importCustomizer.addStarImports( viewConfiguration.packageImports )
        importCustomizer.addStaticStars( viewConfiguration.staticImports )
        compilerConfiguration.addCompilationCustomizers(
                importCustomizer,
                new ASTTransformationCustomizer(newViewsTransform())
        )
    }

    protected ViewsTransform newViewsTransform() {
        new ViewsTransform(extension, dynamicTemplatePrefix)
    }
}