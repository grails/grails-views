package grails.views

import groovy.text.Template
import groovy.transform.CompileStatic

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class WritableScriptTemplate implements Template {

    Class<? extends WritableScript> templateClass
    File sourceFile

    WritableScriptTemplate(Class<? extends WritableScript> templateClass) {
        this.templateClass = templateClass
    }

    WritableScriptTemplate(Class<? extends WritableScript> templateClass, File sourceFile) {
        this.templateClass = templateClass
        this.sourceFile = sourceFile
    }

    @Override
    Writable make() {
        make Collections.emptyMap()
    }

    @Override
    Writable make(Map binding) {
        def writableTemplate = templateClass
                                    .newInstance()
        if(!binding.isEmpty()) {
            writableTemplate.binding = new Binding(binding)
        }
        writableTemplate.setSourceFile(sourceFile)
        return writableTemplate
    }
}
