package grails.plugin.json.view.api.jsonapi

import org.grails.datastore.mapping.model.PersistentProperty

interface JsonApiIdRenderStrategy {

    /**
     * @param object The instance of the domain class
     * @param identity The persistent property of the identity
     * @return The value to be rendered as JSON
     */
    Object render(Object object, PersistentProperty identity)
}
