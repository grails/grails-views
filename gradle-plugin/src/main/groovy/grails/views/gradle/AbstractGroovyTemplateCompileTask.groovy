package grails.views.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.process.ExecResult
import org.gradle.process.JavaExecSpec
import org.gradle.work.InputChanges

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
    void execute(InputChanges inputs) {
        compile()
    }

    protected void compile() {
        def projectPackageNames = getProjectPackageNames(project.projectDir)

        if(packageName == null) {
            packageName = project.name
            if(!packageName) {
                packageName = project.projectDir.canonicalFile.name
            }
        }

        ExecResult result = project.javaexec(
                new Action<JavaExecSpec>() {
                    @Override
                    @CompileDynamic
                    void execute(JavaExecSpec javaExecSpec) {
                        javaExecSpec.mainClass.set(getCompilerName())
                        javaExecSpec.setClasspath(getClasspath())

                        def jvmArgs = compileOptions.forkOptions.jvmArgs
                        if(jvmArgs) {
                            javaExecSpec.jvmArgs(jvmArgs)
                        }
                        javaExecSpec.setMaxHeapSize( compileOptions.forkOptions.memoryMaximumSize )
                        javaExecSpec.setMinHeapSize( compileOptions.forkOptions.memoryInitialSize )


                        String packageImports = projectPackageNames.join(',') ?: packageName
                        def arguments = [
                                srcDir.canonicalPath,
                                destinationDirectory.getAsFile().get()?.canonicalPath,
                                targetCompatibility,
                                packageImports,
                                packageName,
                                project.file("grails-app/conf/application.yml").canonicalPath,
                                compileOptions.encoding
                                ]

                        prepareArguments(arguments)
                        javaExecSpec.args(arguments)
                    }

                }
        )
        result.assertNormalExitValue()

    }

    void prepareArguments(List<String> arguments) {
        // no-op
    }

    @Input
    protected String getCompilerName() {
        "grails.views.GenericGroovyTemplateCompiler"
    }

    @Input
    abstract String getFileExtension()

    @Input
    abstract String getScriptBaseName()

    Iterable<String> getProjectPackageNames(File baseDir) {
        File rootDir = baseDir ? new File(baseDir, "grails-app${File.separator}domain") : null
        Set<String> packageNames = []
        if (rootDir?.exists()) {
            populatePackages(rootDir, packageNames, "")
        }
        return packageNames
    }

    protected populatePackages(File rootDir, Collection<String> packageNames, String prefix) {
        rootDir.eachDir { File dir ->
            def dirName = dir.name
            if (!dir.hidden && !dirName.startsWith('.')) {
                packageNames << "${prefix}${dirName}".toString()

                populatePackages(dir, packageNames, "${prefix}${dirName}.")
            }
        }
    }
}
