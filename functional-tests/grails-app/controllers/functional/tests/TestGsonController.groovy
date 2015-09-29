package functional.tests

class TestGsonController {

    def testRespond() {
        def test = new Test(name:"Bob")
        respond test
    }

    def testRespondWithTemplateForDomain() {
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

    def testGsonFromPlugin() {
        render view:"/fromPlugin"
    }

    def testLinks() {

    }
}
