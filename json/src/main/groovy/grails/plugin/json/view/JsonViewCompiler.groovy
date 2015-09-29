package grails.plugin.json.view

import grails.config.ConfigMap
import grails.plugin.json.view.internal.JsonViewsTransform
import grails.views.AbstractGroovyTemplateCompiler
import grails.views.GenericViewConfiguration
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


    @Override
    protected CompilerConfiguration configureCompiler() {
        def compiler = super.configureCompiler()
        if(viewConfiguration.compileStatic) {
            configuration.addCompilationCustomizers(
                    new ASTTransformationCustomizer(Collections.singletonMap("extensions", "grails.plugin.json.view.internal.JsonTemplateTypeCheckingExtension"), CompileStatic.class));
        }
        configuration.setScriptBaseClass(
                viewConfiguration.baseTemplateClass.name
        )
        return compiler
    }

    @Override
    protected ViewsTransform newViewsTransform() {
        return new JsonViewsTransform(this.viewConfiguration.extension)
    }


    static void main(String[] args) {
        if(args.length != 7) {
            System.err.println("Invalid arguments")
            System.err.println("""
Usage: java -cp CLASSPATH ${JsonViewCompiler.name} [srcDir] [destDir] [targetCompatibility] [packageImports] [packageName] [configFile] [encoding]
""")
            System.exit(1)
        }
        File srcDir = new File(args[0])
        File destinationDir = new File(args[1])
        String targetCompatibility = args[2]
        String[] packageImports = args[3].split(',')
        String packageName = args[4]
        File configFile = new File(args[5])
        String encoding = new File(args[6])

        def configuration = new JsonViewConfiguration(
                packageName: packageName,
                packageImports: packageImports,
                encoding: encoding
        )

        configuration.readConfiguration(configFile)

        def compiler = new JsonViewCompiler(configuration, srcDir)
        compiler.setTargetDirectory( destinationDir )
        compiler.setSourceEncoding( configuration.encoding )
        if(targetCompatibility != null) {
            compiler.setTargetBytecode( targetCompatibility )
        }


        def fileExtension = configuration.extension
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
