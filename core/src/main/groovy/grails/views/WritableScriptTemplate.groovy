package grails.views

import groovy.text.Template
import groovy.transform.CompileStatic

/**
 * @author Graeme Rocher
 */
@CompileStatic
class WritableScriptTemplate implements Template {

    Class<? extends WritableScript> templateClass

    WritableScriptTemplate(Class<? extends WritableScript> templateClass) {
        this.templateClass = templateClass
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
        return writableTemplate
    }
}
