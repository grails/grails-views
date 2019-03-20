package grails.views.gradle

import grails.util.GrailsNameUtils
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetOutput
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import org.grails.gradle.plugin.core.GrailsExtension
import org.grails.gradle.plugin.util.SourceSets

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

        AbstractGroovyTemplateCompileTask templateCompileTask = (AbstractGroovyTemplateCompileTask)allTasks.create("compile${upperCaseName}Views".toString(), (Class<? extends Task>)taskClass)


        SourceSet mainSourceSet = SourceSets.findMainSourceSet(project)
        SourceSetOutput output = mainSourceSet?.output
        FileCollection classesDir = resolveClassesDirs(output, project)
        File destDir = new File(project.buildDir, "${templateCompileTask.fileExtension}-classes/main")
        output?.dir(destDir)
        Configuration providedConfig = project.configurations.findByName('provided')


        FileCollection allClasspath

        project.afterEvaluate {
            GrailsExtension grailsExt = project.extensions.getByType(GrailsExtension)
            if (grailsExt.pathingJar && Os.isFamily(Os.FAMILY_WINDOWS)) {
                Jar pathingJar = (Jar) allTasks.findByName('pathingJar')
                allClasspath = project.files("${project.buildDir}/classes/groovy/main", "${project.buildDir}/resources/main", "${project.projectDir}/gsp-classes", pathingJar.archivePath)
                templateCompileTask.dependsOn(pathingJar)
                templateCompileTask.setClasspath(allClasspath)
            }
        }

        allClasspath = classesDir + project.configurations.getByName('compile')
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
                if (jar.name == 'bootJar') {
                    jar.dependsOn templateCompileTask
                    jar.from(destDir) { CopySpec spec ->
                        spec.into("BOOT-INF/classes")
                    }
                } else if(jar.name == 'jar') {
                    jar.dependsOn templateCompileTask
                    jar.from destDir
                }
            }
        }
    }

    @CompileDynamic
    protected FileCollection resolveClassesDirs(SourceSetOutput output, Project project) {
        return output.classesDirs ?: project.files(new File(project.buildDir, "classes/groovy/main"))
    }

}
