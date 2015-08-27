package grails.views.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.process.ExecResult
import org.gradle.process.JavaExecSpec

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

    @InputDirectory
    File srcDir

    @Nested
    ViewCompileOptions compileOptions = new ViewCompileOptions()

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
        ExecResult result = project.javaexec(
                new Action<JavaExecSpec>() {
                    @Override
                    void execute(JavaExecSpec javaExecSpec) {
                        javaExecSpec.setMain("grails.views.GenericGroovyTemplateCompiler")
                        javaExecSpec.setClasspath(getClasspath())

                        def jvmArgs = compileOptions.forkOptions.jvmArgs
                        if(jvmArgs) {
                            javaExecSpec.jvmArgs(jvmArgs)
                        }
                        javaExecSpec.setMaxHeapSize( compileOptions.forkOptions.memoryMaximumSize )
                        javaExecSpec.setMinHeapSize( compileOptions.forkOptions.memoryInitialSize )
                        javaExecSpec.args(
                                getScriptBaseName(),
                                packageName,
                                getFileExtension(),
                                srcDir.canonicalPath,
                                destinationDir.canonicalPath,
                                targetCompatibility,
                                compileOptions.encoding
                        )
                    }
                }
        )
        result.assertNormalExitValue()
    }

    abstract String getFileExtension()

    abstract String getScriptBaseName()
}
