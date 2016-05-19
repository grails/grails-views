package functional.tests

import grails.mongodb.geo.Point

/**
 * Created by graemerocher on 19/05/16.
 */
class Place {
    String name
    Point location

    static mapWith = "mongo"
}
