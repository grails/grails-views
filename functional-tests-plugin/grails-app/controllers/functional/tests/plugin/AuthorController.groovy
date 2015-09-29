package functional.tests.plugin

import grails.rest.RestfulController

class AuthorController extends RestfulController<Author> {

    static responseFormats = ['json']

    AuthorController() {
        super(Author)
    }

}
