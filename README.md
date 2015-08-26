# Grails Views

Additional View Technologies for Grails

Initial implementation includes JSON views powered by Groovy's JsonBuilder.

## JSON Views

JSON views go into the `grails-app/views` directory and end with the `.gson` suffix. They are regular Groovy scripts and can be opened in any Groovy editor.

Example JSON view:

    json.person {
        name "bob"
    }
    
Produces

    {"person":{"name":"bob"}}

All JSON views implement the [JsonView](json/src/main/groovy/grails/plugins/json/view/api/JsonView.groovy) and [HttpView](core/src/main/groovy/grails/views/api/HttpView.groovy) traits.

There is an implicit `json` variable which is an instance of [StreamingJsonBuilder](http://docs.groovy-lang.org/latest/html/api/groovy/json/StreamingJsonBuilder.html).

Example usages:

    json(1,2,3) == "[1,2,3]"
    json { name "Bob" } == '{"name":"Bob"}'
    json([1,2,3]) { n it } == '[{"n":1},{"n":2},{"n":3}]'

### Static Compilation and Models

JSON views are statically compiled. You can disable static compilation if you prefer by setting `grails.views.json.compileStatic`:

    grails:
        views:
            json:
                compileStatic: false

For model variables you need to declare the types otherwise you will get a compilation error:

    model {
        Person person
    }
    json {
        name person.name
        age person.age
    }

### Links

Links can be generated using the `g.link(..)` method:

    json.person {
        name "bob"
        homepage g.link(controller:"person", id:"bob")
    }

### Templates

You can define templates starting with underscore `_`. For example given the following template called `_person.gsp`:

    model {
        Person person
    }
    json {
        name person.name
        age person.age
    }

You can render it with a view as follows:

    model {
        Family family
    }
    json {
        name family.father.name
        age family.father.age
        oldestChild g.render(template:"person", model:[person: family.children.max { Person p -> p.age } ])
        children g.render(template:"person", collection: family.children, var:'person')
    }

### Headers, Content Type

To customize content types and headers use the `page` object from [HttpView](core/src/main/groovy/grails/views/api/HttpView.groovy):

    page.contentType "application/hal+json"
    page.header "Token", "foo"
    json.person {
        name "bob"
    }



