package grails.plugin.json.view

import grails.core.GrailsClass
import grails.persistence.Entity
import grails.plugin.json.view.api.JsonView
import grails.plugin.json.view.api.internal.JsonGrailsViewHelper
import org.grails.core.DefaultGrailsDomainClass
import org.grails.core.support.GrailsDomainConfigurationUtil
import org.grails.datastore.gorm.config.GrailsDomainClassMappingContext
import org.grails.datastore.gorm.config.GrailsDomainClassPersistentEntity
import org.grails.datastore.mapping.model.MappingContext
import spock.lang.Specification

/*
 * Copyright 2014 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author graemerocher
 */
class JsonViewHelperSpec extends Specification {

    void "Test render object method with customizer"() {
        given:"A view helper"
        def jsonView = Mock(JsonView)
        def mappingContext = Mock(MappingContext)

        def dc1 = new DefaultGrailsDomainClass(Test)
        GrailsDomainConfigurationUtil.configureDomainClassRelationships([dc1] as GrailsClass[], [(dc1.fullName):dc1])

        def entity = new GrailsDomainClassPersistentEntity(dc1, Mock(GrailsDomainClassMappingContext))
        entity.initialize()
        mappingContext.getPersistentEntity(Test.name) >> entity
        jsonView.getMappingContext() >> mappingContext

        def viewHelper = new JsonGrailsViewHelper(jsonView)

        when:"We render an object"

        def test = new Test(title: "The Stand", author: new TestAuthor(name: "Stephen King"))
        test.id = 1L
        def result = viewHelper.render(test) {
            pages 1000
        }
        then:"The result is correct"
        result.toString() == '{"id":1,"author":{"name":"Stephen King"},"title":"The Stand","pages":1000}'
    }

    void "Test render object method"() {
        given:"A view helper"
        def jsonView = Mock(JsonView)
        def mappingContext = Mock(MappingContext)

        def dc1 = new DefaultGrailsDomainClass(Test)
        GrailsDomainConfigurationUtil.configureDomainClassRelationships([dc1] as GrailsClass[], [(dc1.fullName):dc1])

        def entity = new GrailsDomainClassPersistentEntity(dc1, Mock(GrailsDomainClassMappingContext))
        entity.initialize()
        mappingContext.getPersistentEntity(Test.name) >> entity
        jsonView.getMappingContext() >> mappingContext

        def viewHelper = new JsonGrailsViewHelper(jsonView)

        when:"We render an object"

        def test = new Test(title: "The Stand", author: new TestAuthor(name: "Stephen King"))
        test.id = 1L
        def result = viewHelper.render(test)
        then:"The result is correct"
        result.toString() == '{"id":1,"author":{"name":"Stephen King"},"title":"The Stand"}'

        when:"We render an object"
        result = viewHelper.render(new Test(title:"The Stand", author:new TestAuthor(name:"Stephen King")), [excludes:['author']])
        then:"The result is correct"
        result.toString() == '{"title":"The Stand"}'
    }

    void "Test render object method with plain object"() {
        given:"A view helper"
        def jsonView = Mock(JsonView)
        def mappingContext = Mock(MappingContext)

        def dc = new DefaultGrailsDomainClass(Test)

        def entity = new GrailsDomainClassPersistentEntity(dc, Mock(GrailsDomainClassMappingContext))
        entity.initialize()
        mappingContext.getPersistentEntity(Test.name) >> entity
        jsonView.getMappingContext() >> mappingContext

        def viewHelper = new JsonGrailsViewHelper(jsonView)

        when:"We render an object"
        def result = viewHelper.render(new Test2(title:"The Stand", author:"Stephen King"))
        then:"The result is correct"
        result.toString() == '{"author":"Stephen King","title":"The Stand"}'

        when:"We render an object"
        result = viewHelper.render(new Test2(title:"The Stand", author:"Stephen King"), [excludes:['author']])
        then:"The result is correct"
        result.toString() == '{"title":"The Stand"}'
    }
}

@Entity
class Test {
    String title
    TestAuthor author

    static embedded = ['author']
}

class TestAuthor {
    String name
}

class Test2 {
    String title
    String author
}