package grails.views

import grails.views.compiler.ViewsTransform
import groovy.io.FileType
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.io.FileReaderSource

/**
 * A generic compiler for Groovy templates that are compiled into classes in production
 *
 * @author Graeme Rocher
 * @since 1.0
 */
abstract class AbstractGroovyTemplateCompiler {

    @Delegate CompilerConfiguration configuration = new CompilerConfiguration()

    String packageName = ""
    File sourceDir
    final ViewConfiguration viewConfiguration

    AbstractGroovyTemplateCompiler(ViewConfiguration configuration, File sourceDir) {
        this.viewConfiguration = configuration
        this.packageName = configuration.packageName
        this.sourceDir = sourceDir
        configureCompiler()
    }

    protected CompilerConfiguration configureCompiler() {
        configuration.compilationCustomizers.clear()

        def importCustomizer = new ImportCustomizer()
        importCustomizer.addStarImports( viewConfiguration.packageImports )
        configuration.addCompilationCustomizers(importCustomizer)
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(newViewsTransform()))
        return configuration
    }

    protected ViewsTransform newViewsTransform() {
        new ViewsTransform(viewConfiguration.extension)
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

}
