package functional.tests

import grails.transaction.Transactional

class EmbeddedController {

    static responseFormats = ['json']

    @Transactional
    def index() {
        Embedded embedded = new Embedded(name: "Foo", customClass: new CustomClass(name: "Bar"))
        embedded.save(flush: true)
        [embedded: embedded]
    }
}
