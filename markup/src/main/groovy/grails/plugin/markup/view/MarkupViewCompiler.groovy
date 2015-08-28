package grails.plugin.markup.view

import grails.views.GenericGroovyTemplateCompiler
import groovy.io.FileType
import groovy.text.markup.BaseTemplate
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.CompilationCustomizer

/**
 * Created by graemerocher on 28/08/15.
 */
@InheritConstructors
class MarkupViewCompiler extends GenericGroovyTemplateCompiler {

    boolean compileStatic = true

    @Override
    protected CompilerConfiguration configureCompiler() {
        super.configureCompiler()
        def templateCustomizer = (CompilationCustomizer) getClass().classLoader.loadClass("groovy.text.markup.TemplateASTTransformer")
                .newInstance(viewConfiguration)
        configuration.addCompilationCustomizers(templateCustomizer)
        if(compileStatic) {
            configuration.addCompilationCustomizers(
                    new ASTTransformationCustomizer(Collections.singletonMap("extensions", "groovy.text.markup.MarkupTemplateTypeCheckingExtension"), CompileStatic.class));
        }
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
        def configuration = new MarkupViewConfiguration(
                baseTemplateClass: (Class<? extends BaseTemplate>)baseClass,
                packageName: packageName,
                extension: fileExtension
        )

        def compiler = new MarkupViewCompiler(configuration, srcDir)
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

