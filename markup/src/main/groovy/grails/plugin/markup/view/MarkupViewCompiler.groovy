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
        if(args.length != 9) {
            System.err.println("Invalid arguments")
            System.err.println("""
Usage: java -cp CLASSPATH ${MarkupViewCompiler.name} [scriptBaseName] [packageName] [fileExtension] [srcDir] [destinationDir] [targetCompatibility] [encoding] [packageImports]
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
        def configuration = new MarkupViewConfiguration(
                baseTemplateClass: (Class<? extends BaseTemplate>)baseClass,
                packageName: packageName,
                extension: fileExtension,
                compileStatic: compileStatic,
                packageImports: packageImports
        )

        def compiler = new MarkupViewCompiler(configuration, srcDir)
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

