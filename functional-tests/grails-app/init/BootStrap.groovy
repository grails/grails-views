import functional.tests.Circular
import functional.tests.Player
import functional.tests.Team

class BootStrap {

    def init = { servletContext ->

        Team t = new Team(name: "Barcelona")

        def captain = new Player(name: "Iniesta")
        t.captain = captain
        t.players = [captain, new Player(name:"Messi")]
        t.save(flush:true)

        Circular c = new Circular(name: "topLevel")
        c.addToCirculars([name: "topLevel-2"])
        c.addToCirculars([name: "topLevel-3"])
        c.save(flush: true, failOnError: true)

    }
    def destroy = {
    }
}
