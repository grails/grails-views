package functional.tests

class TestController {

    def testRespond() {
        def test = new Test(name:"Bob")
        respond test
    }

    def testCompilationError() {
        [one:"two"]
    }

    def testRuntimeError() {
        [one:"two"]
    }

    def testTemplate() {
        [test: new Test(name:"Bob"), child: new Test(name:"Joe")]
    }

    def testLinks() {

    }
}
