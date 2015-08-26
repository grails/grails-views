package grails.views.gradle

import grails.views.GenericGroovyTemplateCompiler
import groovy.transform.CompileStatic
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

/**
 * Abstract Gradle task for compiling templates, using GenericGroovyTemplateCompiler
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
abstract class AbstractGroovyTemplateCompileTask extends AbstractCompile {
    @Input
    @Optional
    String packageName

    @Input
    @Optional
    String encoding = "UTF-8"

    @InputDirectory
    File srcDir

    @Override
    void setSource(Object source) {
        try {
            srcDir = project.file(source)
            if(srcDir.exists() && !srcDir.isDirectory()) {
                throw new IllegalArgumentException("The source for GSP compilation must be a single directory, but was $source")
            }
            super.setSource(source)
        } catch (e) {
            throw new IllegalArgumentException("The source for GSP compilation must be a single directory, but was $source")
        }
    }

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
        compile()
    }

    @Override
    protected void compile() {
        def compiler = new GenericGroovyTemplateCompiler(getScriptBaseName(),packageName, srcDir)
        compiler.setTargetDirectory( getDestinationDir() )
        compiler.setClasspath( getClasspath().asPath )
        compiler.setSourceEncoding( encoding )
        if(targetCompatibility != null) {
            compiler.setTargetBytecode( targetCompatibility )
        }

        def ext = getFileExtension()
        compiler.setDefaultScriptExtension(ext)
        compiler.compile( project.files(srcDir).filter { File f -> f.name.endsWith(ext) }.files)
    }

    abstract String getFileExtension()

    abstract String getScriptBaseName()
}
