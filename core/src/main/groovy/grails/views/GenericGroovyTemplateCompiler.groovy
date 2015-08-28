package grails.views

import grails.views.compiler.ViewsTransform
import groovy.io.FileType
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.io.FileReaderSource

/**
 * A generic compiler for Groovy templates that are compiled into classes in production
 *
 * @author Graeme Rocher
 * @since 1.0
 */
class GenericGroovyTemplateCompiler {

    @Delegate CompilerConfiguration configuration = new CompilerConfiguration()

    String packageName = ""
    File sourceDir
    final ViewConfiguration viewConfiguration

    GenericGroovyTemplateCompiler(ViewConfiguration configuration, File sourceDir) {
        this.viewConfiguration = configuration
        this.packageName = configuration.packageName
        this.sourceDir = sourceDir
        configureCompiler()
    }

    protected CompilerConfiguration configureCompiler() {
        configuration.compilationCustomizers.clear()
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(new ViewsTransform(viewConfiguration.extension)))
        return configuration
    }

    void compile(Iterable<File> sources) {
        configuration.setClasspathList(classpath)
        def pathToSourceDir = sourceDir.canonicalPath
        for(source in sources) {
            configureCompiler()
            def unit = new CompilationUnit(configuration)
            def pathToSource = source.canonicalPath
            def path = pathToSource - pathToSourceDir
            def templateName = GenericGroovyTemplateResolver.resolveTemplateName(
                    packageName, path
            )
            unit.addSource(new SourceUnit(
                    templateName,
                    new FileReaderSource(source, configuration),
                    configuration,
                    unit.classLoader,
                    unit.errorCollector
            ))
            unit.compile()
        }

    }

    void compile(File...sources) {
        compile Arrays.asList(sources)
    }

    static void main(String[] args) {
        if(args.length != 7) {
            System.err.println("Invalid arguments")
            System.exit(1)
        }
        String scriptBaseName = args[0]
        String packageName = args[1]
        String fileExtension = args[2]
        File srcDir = new File(args[3])
        File destinationDir = new File(args[4])
        String targetCompatibility = args[5]
        String encoding = args[6]


        def baseClass = Thread.currentThread().contextClassLoader.loadClass(scriptBaseName)
        def configuration = new GenericViewConfiguration(
                baseTemplateClass: baseClass,
                packageName: packageName,
                extension: fileExtension
        )
        def compiler = new GenericGroovyTemplateCompiler(configuration, srcDir)
        compiler.setTargetDirectory( destinationDir )
        compiler.setSourceEncoding( encoding )
        if(targetCompatibility != null) {
            compiler.setTargetBytecode( targetCompatibility )
        }

        compiler.setDefaultScriptExtension(fileExtension)

        List<File> allFiles = []
        srcDir.eachFileRecurse(FileType.FILES) { File f ->
            if(f.name.endsWith(fileExtension)) {
                allFiles.add(f)
            }
        }
        compiler.compile(allFiles)
    }
}
