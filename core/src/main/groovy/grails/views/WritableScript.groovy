package grails.views

import groovy.transform.CompileStatic

/**
 * A script that is writable
 *
 * @author Graeme Rocher
 */
@CompileStatic
abstract class WritableScript extends Script implements Writable, WriterProvider {

    Writer out
    File sourceFile

    private Map<String, Class> modelTypes

    /**
     * @return The current writer
     */
    Writer getOut() {
        return out
    }

    @Override
    final Writer writeTo(Writer out) throws IOException {
        this.out = out
        try {
            return doWrite(out)
        } catch (Throwable e) {
            if(ViewsEnvironment.isDevelopmentMode() && sourceFile != null) {
                throw new ViewRenderException("Error rendering view: ${e.message}", e, sourceFile, this)
            }
            else {
                throw new ViewException("Error rendering view: ${e.message}", e)
            }
        }
    }

    /**
     * Subclasses should implement to perform the write
     * @param writer The writer
     * @return The original writer or a wrapped version
     */
    abstract Writer doWrite(Writer writer)

    /**
     * Obtains a model value for the given name and type
     *
     * @param name The name
     * @param targetType The type
     * @return The model value or null if it doesn't exist
     */
    def <T> T model(String name, Class<T> targetType = Object) {
        def value = getBinding().variables.get(name)
        if(targetType.isInstance(value)) {
            return (T)value
        }
        return null
    }

    void setModelTypes(Map<String, Class> modelTypes) {
        this.modelTypes = modelTypes
    }

    Map<String, Class> getModelTypes() {
        return modelTypes
    }
}
