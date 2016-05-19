package functional.tests

import grails.mongodb.geo.Point

/**
 * Created by graemerocher on 19/05/16.
 */
class PlaceController {

    def show() {
        respond new Place(name: "London", location: Point.valueOf(10,10))
    }
}
