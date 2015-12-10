import functional.tests.Player
import functional.tests.Team

class BootStrap {

    def init = { servletContext ->

        Team t = new Team(name: "Barcelona")

        def captain = new Player(name: "Iniesta")
        t.captain = captain
        t.players = [captain, new Player(name:"Messi")]
        t.save(flush:true)
    }
    def destroy = {
    }
}
