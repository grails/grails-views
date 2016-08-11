package grails.plugin.json.view.api.internal

import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.view.api.GrailsJsonViewHelper
import grails.plugin.json.view.api.JsonApiViewHelper
import grails.plugin.json.view.api.JsonView
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

/**
 * @Author Colin Harrington
 */
@CompileStatic
class DefaultJsonApiViewHelper implements JsonApiViewHelper {
    JsonView view
    GrailsJsonViewHelper viewHelper
    String contentType = "application/vnd.api+json"

    public static final JsonOutput.JsonWritable NULL_OUTPUT = new JsonOutput.JsonWritable() {
        @Override
        Writer writeTo(Writer out) throws IOException {
            out.write(JsonOutput.NULL_VALUE);
            return out;
        }
    }

    public static final JsonOutput.JsonWritable NOOP_OUTPUT = new JsonOutput.JsonWritable() {
        @Override
        Writer writeTo(Writer out) throws IOException {
            return out;
        }
    }

    DefaultJsonApiViewHelper(JsonView view, GrailsJsonViewHelper viewHelper) {
        this.view = view
        this.viewHelper = viewHelper
    }

    @Override
    JsonOutput.JsonWritable render(Object object) {
        if (object == null) {
            return NULL_OUTPUT
        }
        //TODO impl
        JsonOutput.JsonWritable jsonWritable = new JsonOutput.JsonWritable() {
            @Override
            @CompileStatic
            Writer writeTo(Writer out) throws IOException {
                out.write(JsonOutput.OPEN_BRACE)
                //jsonapi tag
//                JsonOutput.JsonWritable jsonapi = jsonapiMember()
//                jsonapi.writeTo(out)

                renderData(object).writeTo(out)

                out.write(JsonOutput.CLOSE_BRACE)
                return out
            }


        }
        return jsonWritable
    }

    JsonOutput.JsonWritable renderData(Object object) {
        JsonOutput.JsonWritable writable = new JsonOutput.JsonWritable() {

            @Override
            Writer writeTo(Writer out) throws IOException {
                out.write(JsonOutput.toJson("data"))
                out.write(JsonOutput.COLON);

                PersistentEntity entity = findEntity(object)
                println entity?.dump()
                out.write(JsonOutput.OPEN_BRACE)

                out.write(JsonOutput.toJson('type'))
                out.write(JsonOutput.COLON)
                out.write(JsonOutput.toJson(entity.decapitalizedName))
                out.write(JsonOutput.COMMA)

                out.write(JsonOutput.toJson('id'))
                out.write(JsonOutput.COLON)

                def linkGenerator = view.linkGenerator
                out.write(JsonOutput.toJson(linkGenerator.link(resource: object))) //TODO Use a linking strategy

                if (entity.persistentProperties) {
                    out.write(JsonOutput.COMMA)
                    out.write(JsonOutput.toJson('attributes'))
                    out.write(JsonOutput.COLON);
                    out.write(JsonOutput.OPEN_BRACE)

                    entity.persistentProperties.eachWithIndex { PersistentProperty persistentProperty, int idx ->
                        out.write(JsonOutput.toJson(persistentProperty.name))
                        out.write(JsonOutput.COLON);
                        out.write(JsonOutput.toJson(((GroovyObject) object).getProperty(persistentProperty.name)))
                        if (idx < entity.persistentProperties.size() - 1) {
                            out.write(JsonOutput.COMMA)
                        }
                    }
                    //TODO relationships
                    out.write(JsonOutput.CLOSE_BRACE)
                }
                out.write(JsonOutput.CLOSE_BRACE)
                return out
            }
        }
        return writable
    }

    JsonOutput.JsonWritable jsonapiMember() {
        if (false) {
            JsonOutput.JsonWritable writable = new JsonOutput.JsonWritable() {
                @Override
                @CompileStatic
                Writer writeTo(Writer out) throws IOException {
                    out.write(JsonOutput.OPEN_BRACE)
                    StreamingJsonBuilder builder = new StreamingJsonBuilder(out)
                    builder.call {
                        writeName("jsonapi")
                        writeValue([version: '1.0'])
                    }
                    out.write(JsonOutput.CLOSE_BRACE)
                    return out
                }

            }

            return writable
        } else {
            return NOOP_OUTPUT
        }
    }

    protected PersistentEntity findEntity(Object object) {
        def clazz = object.getClass()
        try {
            return GormEnhancer.findEntity(clazz)
        } catch (Throwable e) {
            return ((JsonView) view)?.mappingContext?.getPersistentEntity(clazz.name)
        }
    }
}
