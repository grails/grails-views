package grails.plugin.json.view.api.internal

import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.view.api.GrailsJsonViewHelper
import grails.plugin.json.view.api.JsonApiViewHelper
import grails.plugin.json.view.api.JsonView
import grails.plugin.json.view.api.json.DefaultJsonApiIdGenerator
import grails.util.Holders
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Simple
import org.springframework.beans.factory.NoSuchBeanDefinitionException

/**
 * @Author Colin Harrington
 */
@CompileStatic
class DefaultJsonApiViewHelper implements JsonApiViewHelper {
    JsonView view
    GrailsJsonViewHelper viewHelper
    String contentType = "application/vnd.api+json"
    boolean exposeJsonApi = false

    JsonApiIdGenerator jsonApiIdGenerator

    public static final JsonOutput.JsonWritable NULL_OUTPUT = new JsonOutput.JsonWritable() {
        @Override
        Writer writeTo(Writer out) throws IOException {
            out.write(JsonOutput.NULL_VALUE)
            return out
        }
    }

    public static final JsonOutput.JsonWritable NOOP_OUTPUT = new JsonOutput.JsonWritable() {
        @Override
        Writer writeTo(Writer out) throws IOException {
            return out
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
        JsonOutput.JsonWritable jsonWritable = new JsonOutput.JsonWritable() {
            @Override
            @CompileStatic
            Writer writeTo(Writer out) throws IOException {
                out.write(JsonOutput.OPEN_BRACE)
                if (exposeJsonApi) {
                    jsonapiMember().writeTo(out)
                }
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
                out.write(JsonOutput.COLON)

                PersistentEntity entity = findEntity(object)
                out.write(JsonOutput.OPEN_BRACE)

                out.write(JsonOutput.toJson('type'))
                out.write(JsonOutput.COLON)
                out.write(JsonOutput.toJson(entity.decapitalizedName))
                out.write(JsonOutput.COMMA)

                out.write(JsonOutput.toJson('id'))
                out.write(JsonOutput.COLON)
                JsonApiIdGenerator idGenerator = getIdGenerator()
                out.write(JsonOutput.toJson(idGenerator.generateId(object)))

                if (entity.persistentProperties) {
                    List<PersistentProperty> attributes = entity.persistentProperties.findAll { it instanceof Simple }
                    if (attributes) {
                        out.write(JsonOutput.COMMA)
                        out.write(JsonOutput.toJson('attributes'))
                        out.write(JsonOutput.COLON)
                        out.write(JsonOutput.OPEN_BRACE)

                        attributes.eachWithIndex { PersistentProperty persistentProperty, int idx ->
                            out.write(JsonOutput.toJson(persistentProperty.name))
                            out.write(JsonOutput.COLON)
                            out.write(JsonOutput.toJson(((GroovyObject) object).getProperty(persistentProperty.name)))
                            if (idx < attributes.size() - 1) {
                                out.write(JsonOutput.COMMA)
                            }
                        }
                        out.write(JsonOutput.CLOSE_BRACE)
                    }
                    //TODO Links
                    if (entity.associations) {
                        out.write(JsonOutput.COMMA)
                        out.write(JsonOutput.toJson('relationships'))
                        out.write(JsonOutput.COLON)
                        out.write(JsonOutput.OPEN_BRACE)
                        entity.associations.eachWithIndex { Association association, int idx ->
                            out.write(JsonOutput.toJson(association.name))
                            out.write(JsonOutput.COLON)

                            out.write(JsonOutput.OPEN_BRACE)
                            out.write(JsonOutput.toJson("data"))
                            out.write(JsonOutput.COLON)
                            if (association.isList()) {
                                out.write(JsonOutput.OPEN_BRACKET)
                                //TODO Refactor and handle list of associations
                                out.write(JsonOutput.CLOSE_BRACKET)

                            } else {
                                out.write(JsonOutput.OPEN_BRACE)

                                out.write(JsonOutput.toJson('type'))
                                out.write(JsonOutput.COLON)
                                out.write(JsonOutput.toJson(association.associatedEntity.decapitalizedName))
                                out.write(JsonOutput.COMMA)

                                out.write(JsonOutput.toJson('id'))
                                out.write(JsonOutput.COLON)
                                out.write(JsonOutput.toJson(idGenerator.generateId(object.properties[association.name])))

                                out.write(JsonOutput.CLOSE_BRACE)
                            }
                            out.write(JsonOutput.CLOSE_BRACE)
                        }
                        out.write(JsonOutput.CLOSE_BRACE)
                    }
                }
                out.write(JsonOutput.CLOSE_BRACE)
                return out
            }
        }
        return writable
    }

    JsonOutput.JsonWritable jsonapiMember() {
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
    }

    protected PersistentEntity findEntity(Object object) {
        def clazz = object.getClass()
        try {
            return GormEnhancer.findEntity(clazz)
        } catch (Throwable e) {
            return ((JsonView) view)?.mappingContext?.getPersistentEntity(clazz.name)
        }
    }

    public JsonApiIdGenerator getIdGenerator() {
        if (jsonApiIdGenerator == null) {
            try {
                this.jsonApiIdGenerator = Holders.getApplicationContext().getBean("jsonApiIdGenerator") as JsonApiIdGenerator
            } catch (NoSuchBeanDefinitionException nsbde) {
                this.jsonApiIdGenerator = new DefaultJsonApiIdGenerator()
            }
        }
        return jsonApiIdGenerator
    }
}
