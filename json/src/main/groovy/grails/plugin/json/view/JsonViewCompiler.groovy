package grails.plugin.json.view

import grails.plugin.json.view.internal.JsonViewsTransform
import grails.views.AbstractGroovyTemplateCompiler
import grails.views.compiler.ViewsTransform
import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

/**
 * A compiler for JSON views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
@InheritConstructors
class JsonViewCompiler extends AbstractGroovyTemplateCompiler {

    boolean compileStatic = true

    @Override
    protected CompilerConfiguration configureCompiler() {
        super.configureCompiler()
        if(compileStatic) {
            configuration.addCompilationCustomizers(
                    new ASTTransformationCustomizer(Collections.singletonMap("extensions", "grails.plugin.json.view.internal.JsonTemplateTypeCheckingExtension"), CompileStatic.class));
        }
        configuration.setScriptBaseClass(
                viewConfiguration.baseTemplateClass.name
        )

    }

    @Override
    protected ViewsTransform newViewsTransform() {
        return new JsonViewsTransform(this.viewConfiguration.extension)
    }


    static void main(String[] args) {
        if(args.length != 9) {
            System.err.println("Invalid arguments")
            System.err.println("""
Usage: java -cp CLASSPATH ${JsonViewCompiler.name} [scriptBaseName] [packageName] [fileExtension] [srcDir] [destinationDir] [targetCompatibility] [encoding] [packageImports]
""")
            System.exit(1)
        }
        String scriptBaseName = args[0]
        String packageName = args[1]
        String fileExtension = args[2]
        File srcDir = new File(args[3])
        File destinationDir = new File(args[4])
        String targetCompatibility = args[5]
        String encoding = args[6]
        String[] packageImports = args[7].split(',')
        boolean compileStatic = Boolean.valueOf(args[8])

        def baseClass = Thread.currentThread().contextClassLoader.loadClass(scriptBaseName)
        def configuration = new JsonViewConfiguration(
                baseTemplateClass: baseClass,
                packageName: packageName,
                extension: fileExtension,
                packageImports: packageImports
        )

        def compiler = new JsonViewCompiler(configuration, srcDir)
        compiler.setCompileStatic(compileStatic)
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
