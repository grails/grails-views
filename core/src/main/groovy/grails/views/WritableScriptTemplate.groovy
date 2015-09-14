package grails.views

import grails.util.GrailsNameUtils
import grails.views.api.GrailsView
import groovy.text.Template
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.cglib.reflect.FastMethod

import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class WritableScriptTemplate implements Template {

    Class<? extends GrailsView> templateClass
    File sourceFile
    boolean prettyPrint = false

    protected final Map<String, Method> modelSetters = [:]

    WritableScriptTemplate(Class<? extends GrailsView> templateClass) {
        this(templateClass, null)
    }

    WritableScriptTemplate(Class<? extends GrailsView> templateClass, File sourceFile) {
        this.templateClass = templateClass
        this.sourceFile = sourceFile

        initModelTypes(templateClass)
    }

    protected void initModelTypes(Class<? extends WritableScript> templateClass) {
        try {
            def field = templateClass.getDeclaredField(Views.MODEL_TYPES_FIELD)
            field.setAccessible(true)

            def modelTypes = (Map<String, Class>) field.get(templateClass)
            if(modelTypes != null) {
                for(mt in modelTypes) {
                    def propertyName = mt.key
                    def setterName = GrailsNameUtils.getSetterName(propertyName)
                    def method = templateClass.getDeclaredMethod(setterName, mt.value)
                    method.setAccessible(true)
                    modelSetters[propertyName] = method
                }
            }
        } catch (Throwable e) {
            // ignore
        }
    }

    @Override
    Writable make() {
        make Collections.emptyMap()
    }

    @Override
    Writable make(Map binding) {
        WritableScript writableTemplate = templateClass
                                    .newInstance()
        writableTemplate.prettyPrint = prettyPrint
        if(!binding.isEmpty()) {
            writableTemplate.binding = new Binding(binding)
            for(modelSetter in modelSetters.entrySet()) {
                def value = binding[modelSetter.key]
                if(value != null) {
                    modelSetter.value.invoke(writableTemplate, value)
                }
            }
        }
        writableTemplate.setSourceFile(sourceFile)
        return writableTemplate
    }
}
