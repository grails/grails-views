package functional.tests

class ProjectController {

    def index() {
        respond(project: Project.list(fetch: [employees: 'join']).get(0))
    }
}
