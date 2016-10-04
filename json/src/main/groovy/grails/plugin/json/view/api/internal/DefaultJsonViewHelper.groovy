package grails.plugin.json.view.api.internal

import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.view.api.JsonView
import grails.views.api.internal.DefaultGrailsViewHelper
import grails.views.utils.ViewUtils
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.codehaus.groovy.runtime.StackTraceUtils
import org.grails.core.util.IncludeExcludeSupport
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.model.MappingFactory
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.util.ReflectionUtils

import java.beans.PropertyDescriptor
import java.lang.reflect.ParameterizedType

@CompileStatic
@InheritConstructors
class DefaultJsonViewHelper extends DefaultGrailsViewHelper {

    protected final Set<String> TO_STRING_TYPES = [
            "org.bson.types.ObjectId"
    ] as Set

    protected final JsonOutput.JsonWritable NULL_OUTPUT = new JsonOutput.JsonWritable() {
        @Override
        Writer writeTo(Writer out) throws IOException {
            out.write(JsonOutput.NULL_VALUE);
            return out;
        }
    }

    /**
     * Default includes/excludes for GORM properties
     */
    IncludeExcludeSupport<String> includeExcludeSupport = new IncludeExcludeSupport<String>(null, ["class", 'metaClass', 'properties', "version", "attached", "errors", "dirty"]) {
        @Override
        boolean shouldInclude(List<String> incs, List excs, String object) {
            def i = object.lastIndexOf('.')
            String unqualified = i > -1 ? object.substring(i + 1) : null
            return super.shouldInclude(incs, excs, object) && (unqualified == null || (includes(defaultIncludes, unqualified) && !excludes(defaultExcludes, unqualified)))
        }
    }

    List<String> getIncludes(Map arguments) {
        ViewUtils.getStringListFromMap(IncludeExcludeSupport.INCLUDES_PROPERTY, arguments, null)
    }

    List<String> getExcludes(Map arguments) {
        ViewUtils.getStringListFromMap(IncludeExcludeSupport.EXCLUDES_PROPERTY, arguments)
    }

    protected PersistentEntity findEntity(Object object) {
        def clazz = object.getClass()
        try {
            return GormEnhancer.findEntity(clazz)
        } catch (Throwable e) {
            return ((JsonView)view)?.mappingContext?.getPersistentEntity(clazz.name)
        }
    }


    protected Class getGenericType(Class declaringClass, PropertyDescriptor descriptor) {
        def field = ReflectionUtils.findField(declaringClass, descriptor.getName())
        if(field != null) {

            def type = field.genericType
            if(type instanceof ParameterizedType) {
                def args = ((ParameterizedType) type).getActualTypeArguments()
                if(args.length > 0) {
                    def t = args[0]
                    if(t instanceof Class) {
                        return (Class)t
                    }
                }
            }
        }
        return Object
    }

    boolean isStringType(Class propertyType) {
        return TO_STRING_TYPES.contains(propertyType.name)
    }

    boolean isSimpleType(Class propertyType, value) {
        MappingFactory.isSimpleType(propertyType.name) || (value instanceof Enum) || (value instanceof Map)
    }

    protected List<Object> getJsonStackTrace(Throwable e) {
        StackTraceUtils.sanitize(e)
        (List<Object>) e.stackTrace
                .findAll() { StackTraceElement element -> element.lineNumber > -1 }
                .collect() { StackTraceElement element ->
            "$element.lineNumber | ${element.className}.$element.methodName".toString()
        }.toList()
    }

}
