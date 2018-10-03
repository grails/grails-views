package functional.tests

class VehicleController {

    static responseFormats = ['json', 'xml']

    def list() {
        respond(Vehicle.list())
    }

    def garage() {
        respond(garage: Garage.list(fetch: [vehicles: 'join']).get(0))
    }
}
