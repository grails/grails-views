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


    def listExcludesRespond() {
        respond([books: listAllResources(params)])
    }

    def listCallsTmpl() {
        respond([books: listAllResources(params)])
    }

    def listCallsTmplVar() {
        respond([books: listAllResources(params)])
    }

    def listCallsTmplExtraData() {
        respond([books: listAllResources(params)])
    }

    def nonStandardTemplate() {
        respond([book: new Book(title: 'template found'), custom: new CustomClass(name: "Sally")], view:'/non-standard/template')
    }
}
