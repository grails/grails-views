package grails.views.gradle

import grails.util.GrailsNameUtils
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetOutput
import org.gradle.api.tasks.bundling.Jar
import org.grails.gradle.plugin.core.GrailsExtension
import org.grails.gradle.plugin.core.IntegrationTestGradlePlugin
import org.grails.gradle.plugin.util.SourceSets
import org.springframework.boot.gradle.plugin.ResolveMainClassName
import org.springframework.boot.gradle.plugin.SpringBootPlugin

/**
 * Abstract implementation of a plugin that compiles views
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
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
        AbstractGroovyTemplateCompileTask templateCompileTask = (AbstractGroovyTemplateCompileTask) allTasks.register("compile${upperCaseName}Views".toString(), (Class<? extends Task>) taskClass).get()
        SourceSetOutput output = SourceSets.findMainSourceSet(project)?.output
        FileCollection classesDir = resolveClassesDirs(output, project)
        File destDir = new File(project.buildDir, "${templateCompileTask.fileExtension}-classes/main")
        output?.dir(destDir)
        def allClasspath
        project.afterEvaluate {
            GrailsExtension grailsExt = project.extensions.getByType(GrailsExtension)
            if (grailsExt.pathingJar && Os.isFamily(Os.FAMILY_WINDOWS)) {
                Jar pathingJar = (Jar) allTasks.named('pathingJar').get()
                allClasspath = project.files("${project.buildDir}/classes/groovy/main", "${project.buildDir}/resources/main", "${project.projectDir}/gsp-classes", pathingJar.archiveFile.get().asFile)
                templateCompileTask.dependsOn(pathingJar)
                templateCompileTask.setClasspath(allClasspath)
            }
        }
        allClasspath = classesDir + project.configurations.named('compileClasspath').get()
        templateCompileTask.getDestinationDirectory().set( destDir )
        templateCompileTask.setClasspath( allClasspath )
        templateCompileTask.setPackageName(project.name)
        templateCompileTask.setSource(project.file("${project.projectDir}/$pathToSource"))
        templateCompileTask.dependsOn( allTasks.named('classes').get() )
        project.plugins.withType(SpringBootPlugin).configureEach {plugin ->
            allTasks.withType(Jar).configureEach { Task task ->
                if (task.name in ['jar', 'bootJar', 'war', 'bootWar']) { task.dependsOn templateCompileTask }
            }
            allTasks.withType(ResolveMainClassName).configureEach { t -> t.dependsOn(templateCompileTask)}
        }
        project.plugins.withType(IntegrationTestGradlePlugin).configureEach { plugin ->
            allTasks.named("compileIntegrationTestGroovy") { t-> t.dependsOn(templateCompileTask)}
            allTasks.named("integrationTest") {t -> t.dependsOn(templateCompileTask)}
        }
    }

    @CompileDynamic
    protected FileCollection resolveClassesDirs(SourceSetOutput output, Project project) {
        return output.classesDirs ?: project.files(new File(project.buildDir, "classes/groovy/main"))
    }

}
