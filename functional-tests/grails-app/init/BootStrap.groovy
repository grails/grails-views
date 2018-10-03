import functional.tests.Bus
import functional.tests.Car
import functional.tests.Circular
import functional.tests.Garage
import functional.tests.MyEnum
import functional.tests.Player
import functional.tests.Team
import functional.tests.Proxy

class BootStrap {

    def init = { servletContext ->

        Team t = new Team(name: "Barcelona")

        def captain = new Player(name: "Iniesta")
        t.captain = captain
        t.players = [captain, new Player(name:"Messi")]
        t.save(flush:true)

        Circular c = new Circular(name: "topLevel", myEnum: MyEnum.BAR)
        c.addToCirculars([name: "topLevel-2", myEnum: MyEnum.FOO])
        c.addToCirculars([name: "topLevel-3", myEnum: MyEnum.BAR])
        c.save(flush: true, failOnError: true)

        new Proxy(name: "Sally").save(flush: true, failOnError: true)

        new Garage(owner: "Jay Leno")
            .addToVehicles(new Bus(maxPassengers: 30, route: "around town"))
            .addToVehicles(new Car(maxPassengers: 4, make: "Subaru", model: "WRX", year: 2016))
            .save(flush: true, failOnError: true)

    }
    def destroy = {
    }
}
