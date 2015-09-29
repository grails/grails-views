package grails.plugin.markup.view

import grails.plugin.markup.view.internal.MarkupViewsTransform
import grails.views.AbstractGroovyTemplateCompiler
import grails.views.compiler.ViewsTransform
import groovy.io.FileType
import groovy.text.markup.BaseTemplate
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.CompilationCustomizer
/**
 * A compiler for markup templates
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@InheritConstructors
@CompileStatic
class MarkupViewCompiler extends AbstractGroovyTemplateCompiler {

    @Override
    protected CompilerConfiguration configureCompiler() {
        super.configureCompiler()
        def templateCustomizer = (CompilationCustomizer) getClass().classLoader.loadClass("groovy.text.markup.TemplateASTTransformer")
                .newInstance(viewConfiguration)
        configuration.addCompilationCustomizers(templateCustomizer)
        if(viewConfiguration.compileStatic) {
            configuration.addCompilationCustomizers(
                    new ASTTransformationCustomizer(Collections.singletonMap("extensions", "groovy.text.markup.MarkupTemplateTypeCheckingExtension"), CompileStatic.class));
        }
    }

    @Override
    protected ViewsTransform newViewsTransform() {
        return new MarkupViewsTransform(viewConfiguration.extension)
    }

    static void main(String[] args) {
        if(args.length != 7) {
            System.err.println("Invalid arguments")
            System.err.println("""
Usage: java -cp CLASSPATH ${MarkupViewCompiler.name} [srcDir] [destDir] [targetCompatibility] [packageImports] [packageName] [configFile] [encoding]
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


        def configuration = new MarkupViewConfiguration(
                packageName: packageName,
                packageImports: packageImports,
                encoding: encoding
        )
        configuration.readConfiguration(configFile)

        def compiler = new MarkupViewCompiler(configuration, srcDir)
        compiler.setTargetDirectory( destinationDir )
        compiler.setSourceEncoding( encoding )
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

