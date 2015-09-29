package grails.views.gradle

import org.gradle.api.tasks.compile.GroovyForkOptions

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class ViewCompileOptions {

    String encoding = "UTF-8"

    GroovyForkOptions forkOptions = new GroovyForkOptions()

}
