package grails.views.gradle

import grails.util.GrailsNameUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import org.gradle.api.tasks.compile.GroovyCompile
import org.grails.gradle.plugin.util.SourceSets

/**
 * Abstract implementation of a plugin that compiles views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
class AbstractGroovyTemplatePlugin implements Plugin<Project> {

    final Class<? extends AbstractGroovyTemplateCompileTask> taskClass
    final String fileExtension
    final String pathToSource

    AbstractGroovyTemplatePlugin(Class<? extends AbstractGroovyTemplateCompileTask> taskClass, String fileExtension) {
        this.taskClass = taskClass
        this.fileExtension = fileExtension
        this.pathToSource = "grails-app/views"
    }

    AbstractGroovyTemplatePlugin(Class<? extends AbstractGroovyTemplateCompileTask> taskClass, String fileExtension, String pathToSource) {
        this.taskClass = taskClass
        this.fileExtension = fileExtension
        this.pathToSource = pathToSource
    }

    @Override
    void apply(Project project) {
        def allTasks = project.tasks
        def upperCaseName = GrailsNameUtils.getClassName(fileExtension)

        AbstractGroovyTemplateCompileTask templateCompileTask = allTasks.create("compile${upperCaseName}Views", taskClass)


        def mainSourceSet = SourceSets.findMainSourceSet(project)
        def output = mainSourceSet?.output
        def classesDir = output?.classesDir ?: new File(project.buildDir, "classes/main")
        def destDir = output?.dir("${templateCompileTask.fileExtension}-classes") ?: new File(project.buildDir, "${templateCompileTask.fileExtension}-classes/main")
        def providedConfig = project.configurations.findByName('provided')
        def allClasspath = project.configurations.compile + project.files(classesDir)
        if(providedConfig) {
            allClasspath += providedConfig
        }

        templateCompileTask.setDestinationDir( destDir )
        templateCompileTask.setClasspath( allClasspath )
        templateCompileTask.setPackageName(
                project.name
        )
        templateCompileTask.setSource(
                project.file("${project.projectDir}/$pathToSource")
        )

        templateCompileTask.dependsOn( allTasks.findByName('classes') )

        allTasks.withType(War) { War war ->
            war.dependsOn templateCompileTask
            war.classpath = war.classpath + project.files(destDir)
        }
        allTasks.withType(Jar) { Jar jar ->
            if(!(jar instanceof War)) {
                jar.dependsOn templateCompileTask
                jar.from destDir
            }
        }
    }
}
