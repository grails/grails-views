package grails.plugin.json.view.test

import functional.tests.Circular
import functional.tests.MyEnum
import functional.tests.Player
import grails.test.mixin.Mock
import grails.util.TypeConvertingMap
import grails.views.api.http.Parameters
import grails.views.mvc.http.DelegatingParameters
import spock.lang.Specification

/**
 * Created by graemerocher on 14/10/16.
 */
@Mock(Circular)
class JsonViewTestSpec extends Specification implements JsonViewTest {

    void "test render json view"() {
        when:"a view is rendered"
        def result = render(template: '/player/player', model: [player: new Player(name: "Cantona")])

        then:"the result is correct"
        result.json.name == "Cantona"
        result.json.sport == 'football'
    }

    void "test render json view with nested expand"() {
        when: "a view is rendered"
        def parameters = [expand: ['circulars.parent']]

        Parameters params = new DelegatingParameters(new TypeConvertingMap(parameters))
        Circular parent1 = new Circular(name: "Parent 1", myEnum: MyEnum.BAR)
        parent1.save()
        Circular child1 = new Circular(name: 'Child 1', myEnum: MyEnum.BAR, parent: parent1)
        child1.save()
        Circular innerChild1 = new Circular(name: 'Inner Child 1', myEnum: MyEnum.BAR, parent: child1)
        innerChild1.save()
        child1.setCirculars(new LinkedHashSet<>([innerChild1]))
        child1.save()
        Circular child2 = new Circular(name: 'Child 1', myEnum: MyEnum.BAR, parent: parent1)
        child2.save()
        parent1.setCirculars(new LinkedHashSet<>([child1, child2]))
        parent1.save()
        Circular parent2 = new Circular(name: "Parent 2", myEnum: MyEnum.BAR)
        parent2.save()
        List<Circular> circularList = [parent1, parent2]
        def result = render(view: '/circular/index', model: [circularList: circularList, params: params])
        then: "the result is correct"
        result.jsonText == '[{"id":1,"circulars":[{"id":2,"circulars":[{"id":3}],"myEnum":"BAR","name":"Child 1","parent":{"id":1,"circulars":[{"id":2},{"id":4}],"myEnum":"BAR","name":"Parent 1"}},{"id":4,"myEnum":"BAR","name":"Child 1","parent":{"id":1,"circulars":[{"id":2},{"id":4}],"myEnum":"BAR","name":"Parent 1"}}],"myEnum":"BAR","name":"Parent 1"},{"id":5,"myEnum":"BAR","name":"Parent 2"}]'
    }
}
