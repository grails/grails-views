package grails.plugin.json.view.api.internal

import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.view.api.GrailsJsonViewHelper
import grails.plugin.json.view.api.JsonApiViewHelper
import grails.plugin.json.view.api.JsonView
import grails.plugin.json.view.api.jsonapi.DefaultJsonApiIdGenerator
import grails.plugin.json.view.api.jsonapi.JsonApiIdGenerator
import grails.util.Holders
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Basic
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.datastore.mapping.model.types.EmbeddedCollection
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

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
                if (objectHasErrors(object)) {
                    renderErrors(object).writeTo(out)
                } else {
                    renderData(object).writeTo(out)
                    out.write(JsonOutput.COMMA)
                    renderLinks(object).writeTo(out)
                }
                out.write(JsonOutput.CLOSE_BRACE)
                return out
            }
        }
        return jsonWritable
    }

    boolean objectHasErrors(Object subject) {
        if (subject.hasProperty('errors')) {
            Object errors = subject.getAt('errors')
            if (errors instanceof Errors) {
                return errors.hasErrors()
            } else {
                return errors.asBoolean()
            }
        }
        return false
    }

    private boolean isAttributeAssociation(Association a) {
        a instanceof EmbeddedCollection || a instanceof Embedded || a instanceof Basic
    }

    List<Association> getRelationships(PersistentEntity entity) {
        entity.associations.findAll { Association a ->
            !isAttributeAssociation(a)
        }
    }

    List<PersistentProperty> getAttributes(PersistentEntity entity) {
        entity.persistentProperties.findAll { PersistentProperty p ->
            if (p instanceof Association) {
                isAttributeAssociation((Association)p)
            } else {
                true
            }
        }
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
                    List<PersistentProperty> attributes = getAttributes(entity)
                    List<Association> relationShips = getRelationships(entity)

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
                    if (relationShips) {
                        out.write(JsonOutput.COMMA)
                        out.write(JsonOutput.toJson('relationships'))
                        out.write(JsonOutput.COLON)
                        out.write(JsonOutput.OPEN_BRACE)
                        relationShips.eachWithIndex { Association association, int idx ->
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

    JsonOutput.JsonWritable renderErrors(Object object) {
        JsonOutput.JsonWritable writable = new JsonOutput.JsonWritable() {

            @Override
            Writer writeTo(Writer out) throws IOException {
                out.write(JsonOutput.toJson("errors"))
                out.write(JsonOutput.COLON)

                Errors errors = (Errors) object.getAt('errors')

                out.write(JsonOutput.OPEN_BRACKET)

                List<ObjectError> allErrors = errors.allErrors
                allErrors.eachWithIndex { ObjectError error, int idx ->
                    this.writeError(out, error)
                    if (idx < allErrors.size() - 1) {
                        out.write(JsonOutput.COMMA)
                    }
                }

                out.write(JsonOutput.CLOSE_BRACKET)

                return out
            }

            protected writeError(Writer out, ObjectError error) {
                out.write(JsonOutput.OPEN_BRACE)

                out.write(JsonOutput.toJson('code'))
                out.write(JsonOutput.COLON)
                out.write(JsonOutput.toJson(error.code))
                out.write(JsonOutput.COMMA)

                out.write(JsonOutput.toJson('detail'))
                out.write(JsonOutput.COLON)
                out.write(JsonOutput.toJson(viewHelper.message([error: error])))
                out.write(JsonOutput.COMMA)

                out.write(JsonOutput.toJson('source'))
                out.write(JsonOutput.COLON)
                out.write(JsonOutput.OPEN_BRACE)

                out.write(JsonOutput.toJson('object'))
                out.write(JsonOutput.COLON)
                out.write(JsonOutput.toJson(error.getObjectName()))
                out.write(JsonOutput.COMMA)

                if (error instanceof FieldError) {
                    FieldError fieldError = (FieldError) error

                    out.write(JsonOutput.toJson('field'))
                    out.write(JsonOutput.COLON)
                    out.write(JsonOutput.toJson(fieldError.getField()))
                    out.write(JsonOutput.COMMA)

                    out.write(JsonOutput.toJson('rejectedValue'))
                    out.write(JsonOutput.COLON)
                    out.write(JsonOutput.toJson(fieldError.getRejectedValue()))
                    out.write(JsonOutput.COMMA)

                    out.write(JsonOutput.toJson('bindingError'))
                    out.write(JsonOutput.COLON)
                    out.write(JsonOutput.toJson(fieldError.isBindingFailure()))
                }

                out.write(JsonOutput.CLOSE_BRACE)//source
                out.write(JsonOutput.CLOSE_BRACE)//error
            }
        }
        return writable
    }

    JsonOutput.JsonWritable renderLinks(Object object) {
        JsonOutput.JsonWritable writable = new JsonOutput.JsonWritable() {

            @Override
            Writer writeTo(Writer out) throws IOException {
                out.write(JsonOutput.toJson("links"))
                out.write(JsonOutput.COLON)

                out.write(JsonOutput.OPEN_BRACE)
                PersistentEntity entity = findEntity(object)
                out.write(JsonOutput.toJson('self'))
                out.write(JsonOutput.COLON)

                def linkGenerator = view.linkGenerator
                out.write(JsonOutput.toJson(linkGenerator.link(resource: object)))
                List<Association> associations = getRelationships(entity)
                if (associations) {
                    out.write(JsonOutput.COMMA)
                    out.write(JsonOutput.toJson('related'))
                    out.write(JsonOutput.COLON)
                    out.write(JsonOutput.OPEN_BRACE)
                    associations.eachWithIndex { Association association, int idx ->
                        if (!association.isOwningSide()) {
                            def instance = object.properties[association.name]

                            out.write(JsonOutput.toJson("href"))
                            out.write(JsonOutput.COLON)
                            out.write(JsonOutput.toJson(linkGenerator.link(resource: instance)))

                            if (instance instanceof Collection) {
                                Collection instanceCollection = (Collection) instance
                                out.write(JsonOutput.toJson("meta"))
                                out.write(JsonOutput.COLON)
                                out.write(JsonOutput.OPEN_BRACE)

                                Integer count = instanceCollection.size()
                                out.write(JsonOutput.toJson("count"))
                                out.write(JsonOutput.COLON)
                                out.write(JsonOutput.toJson(count))

                                out.write(JsonOutput.CLOSE_BRACE)
                            }
                        }
                    }
                    out.write(JsonOutput.CLOSE_BRACE)
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
