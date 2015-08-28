package grails.plugin.json.view

import grails.views.GenericGroovyTemplateCompiler
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
class JsonViewCompiler extends GenericGroovyTemplateCompiler {

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

    static void main(String[] args) {
        if(args.length != 8) {
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
        boolean compileStatic = Boolean.valueOf(args[7])

        def baseClass = Thread.currentThread().contextClassLoader.loadClass(scriptBaseName)
        def configuration = new JsonViewConfiguration(
                baseTemplateClass: baseClass,
                packageName: packageName,
                extension: fileExtension
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
