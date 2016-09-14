package functional.tests

import grails.rest.RestfulController

class BookController extends RestfulController<Book> {

    static responseFormats = ['json']

    BookController() {
        super(Book)
    }

    def listExcludes() {
        [books: listAllResources(params)]
    }

    def nonStandardTemplate() {
        respond new Book(title: 'template found'), view:'/non-standard/template'
    }
}
