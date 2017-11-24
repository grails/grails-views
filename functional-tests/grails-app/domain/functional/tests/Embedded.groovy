package functional.tests

class Embedded {

    String name
    CustomClass customClass

    static constraints = { }

    static embedded = ['customClass']
}
