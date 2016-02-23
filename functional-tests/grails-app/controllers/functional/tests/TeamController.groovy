package functional.tests


import grails.rest.*
import grails.converters.*

class TeamController extends RestfulController {
    static responseFormats = ['json', 'xml']
    TeamController() {
        super(Team)
    }

    def deep(Long id) {
        respond Team.get(id)
    }

    def hal(Long id) {
        respond Team.get(id)
    }
}
