package grails.views.gradle

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.compile.GroovyForkOptions

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class ViewCompileOptions implements Serializable {

    private static final long serialVersionUID = 0L;

    @Input
    String encoding = "UTF-8"

    @Nested
    GroovyForkOptions forkOptions = new GroovyForkOptions()

}
