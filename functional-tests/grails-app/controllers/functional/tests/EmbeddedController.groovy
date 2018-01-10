package functional.tests

import grails.transaction.Transactional

class EmbeddedController {

    static responseFormats = ['json']

    @Transactional
    def index() {
        Embedded embedded = new Embedded(name: "Foo", customClass: new CustomClass(name: "Bar"), inSameFile: new InSameFile(text: "FooBar"))
        embedded.save(flush: true)
        [embedded: embedded]
    }

    @Transactional
    def jsonapi() {
        Embedded embedded = new Embedded(name: "Foo2", customClass: new CustomClass(name: "Bar2"), inSameFile: new InSameFile(text: "FooBar2"))
        embedded.save(flush: true)
        [embedded: embedded]
    }
}
