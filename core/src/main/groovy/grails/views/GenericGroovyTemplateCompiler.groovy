package grails.views

import grails.views.compiler.ViewsTransform
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

    GenericGroovyTemplateCompiler(String packageName, File sourceDir) {
        this(null, packageName, sourceDir)
    }

    GenericGroovyTemplateCompiler(String scriptBaseName, String packageName, File sourceDir) {
        this.packageName = packageName
        this.sourceDir = sourceDir
        if(scriptBaseName != null) {
            configuration.scriptBaseClass = scriptBaseName
        }

        configuration.addCompilationCustomizers( new ASTTransformationCustomizer(new ViewsTransform()))
    }

    void compile(Iterable<File> sources) {
        def unit = new CompilationUnit(configuration)
        def pathToSourceDir = sourceDir.canonicalPath
        for(source in sources) {
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
        }
        unit.compile()

    }

    void compile(File...sources) {
        compile Arrays.asList(sources)
    }
}
