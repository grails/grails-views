package functional.tests

class CustomerController {

    def index() {
        respond(customer: Customer.list(fetch: [sites: 'join']).get(0))
    }
}
