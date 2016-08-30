package functional.tests

class Circular {

    String name

    static belongsTo = [parent: Circular]

    static hasMany = [circulars: Circular]

    static constraints = {
        parent(nullable: true)
    }
}
