package functional.tests

import grails.rest.RestfulController

class BookController extends RestfulController<Book> {

    static responseFormats = ['json']

    BookController() {
        super(Book)
    }
}
