# Grails Views

Additional View Technologies for Grails 3.0 and above.

Initial implementation includes JSON views powered by Groovy's JsonBuilder, however this project provides the basis for implementation other view types.

## JSON Views


### Installation

Add the following dependency to the `dependencies` block of your `build.gradle`:

    compile "org.grails.plugins:views-json:1.0.0.M1'

To enable Gradle compilation of JSON views for production environment add the following to the `buildscript` `dependencies` block:

    buildscript {
        ...
        dependencies {
            ...
            classpath "org.grails.plugins:views-gradle:1.0.0.M1"
        }
    }

Then apply the `org.grails.plugins.views-json` Gradle plugin after any Grails core gradle plugins:

    ...
    apply plugin: "org.grails.grails-web"
    apply plugin: "org.grails.plugins.views-json"

This will add a `compileGsonViews` task to Gradle.


### Usage

JSON views go into the `grails-app/views` directory and end with the `.gson` suffix. They are regular Groovy scripts and can be opened in any Groovy editor.

Example JSON view:

    json.person {
        name "bob"
    }
    
Produces

    {"person":{"name":"bob"}}

All JSON views implement the [JsonView](json/src/main/groovy/grails/plugin/json/view/api/JsonView.groovy) and [HttpView](core/src/main/groovy/grails/views/api/HttpView.groovy) traits and extend from the [JsonViewTemplate](json/src/main/groovy/grails/plugin/json/view/JsonViewTemplate.groovy) base class.

There is an implicit `json` variable which is an instance of [StreamingJsonBuilder](http://docs.groovy-lang.org/latest/html/api/groovy/json/StreamingJsonBuilder.html).

Example usages:

    json(1,2,3) == "[1,2,3]"
    json { name "Bob" } == '{"name":"Bob"}'
    json([1,2,3]) { n it } == '[{"n":1},{"n":2},{"n":3}]'
    
    
### Rendering Domain Classes
    
Typically your model my involve one or many domain instances. JSON views provide a `render` method for rendering these.
  
For example given the following domain class:

	class Book {
	    String title
	}
    
And the following template: 
 
    model {
        Book book
    }
    
    json g.render(book) 

The resulting output is:

    {id:1,"title":"The Stand"}
    
You can customize the rendering by including or excluding properties:
    
    json g.render(book, [includes:['title']])
    
Or by providing a closure to modify the JSON output:
    
    json g.render(book) {
        pages 1000
    }    

### Configuration

JSON views configuration can be altered with `application.yml`. Any of the properties within the [JsonViewConfiguration](core/src/main/groovy/grails/views/ViewConfiguration.groovy) interface can be set. For example:

    grails:
        views:
            json:
                compileStatic: true
                cache: true
                ...

Alternatively you can register a new `JsonViewConfiguration` bean using the bean name `jsonViewConfiguration` in `resources.groovy`.


### Static Compilation and Models

JSON views are statically compiled. You can disable static compilation if you prefer by setting `grails.views.json.compileStatic`:

    grails:
        views:
            json:
                compileStatic: false

Note: If you disable static compilation rendering performance will suffer.

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

You can define templates starting with underscore `_`. For example given the following template called `_person.gson`:

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

Alternatively for a more concise way to invoke templates, using the `tmpl` variable:

    model {
        Family family
    }
    json {
        name family.father.name
        age family.father.age
        oldestChild tmpl.person( family.children.max { Person p -> p.age } ] )
        children tmpl.person( family.children )
    }

### Headers, Content Type

To customize content types and headers use the `page` object from [HttpView](core/src/main/groovy/grails/views/api/HttpView.groovy):

    page.contentType "application/hal+json"
    page.header "Token", "foo"
    json.person {
        name "bob"
    }

### I18n & Locale Integration

You can lookup i18n messages use the `g.message` method:

    json.error {
        description g.message(code:'default.error.message')
    }

You can also create locale specific views by appending the locale to view name. For example `person_de.gson` for German or `person.gson` for the default.

### Grails Renderer Integration

GSON views integrate with Grails' renderer infrastructure. For example if you create 2 views called `show.gsp` (for HTML) and `show.gson` (for JSON), you can define the following action:

    def show() {
        respond Book.get(params.id)
    }

If you send a request to `/book/show` it will render `show.gsp` but if you send a request to `/book/show.json` it will render `show.gson`.

In addition if you want to define a template to render any instance the `Book` domain classes you can create a `gson` file that matches the class name. For example given a class called `demo.Book` you can create `grails-app/views/demo/Book.gson` and whenever `respond` is called with an instance of `Book` Grails will render `Book.gson`.

### View Resolving Strategy

Grails takes into account a number of factors when attempting to resolve the view including the content type, version and locale.

The following paths are searched:

    * view_name[_LOCALE][_ACCEPT_CONTENT_TYPE][_ACCEPT_VERSION].gson (Example: show_de_hal_v1.0.gson)
    * view_name[_LOCALE][_ACCEPT_CONTENT_TYPE].gson (Example: show_de_hal.gson)
    * view_name[_LOCALE][_ACCEPT_VERSION].gson (Example: show_de_v1.0.gson)
    * view_name[_LOCALE].gson (Example: show_de.gson)
    * view_name[_ACCEPT_CONTENT_TYPE][_ACCEPT_VERSION].gson (Example: show_hal_v1.0.gson)
    * view_name[_ACCEPT_VERSION][_ACCEPT_CONTENT_TYPE].gson (Example: show_v1.0_hal.gson)
    * view_name[_ACCEPT_CONTENT_TYPE].gson (Example: show_hal.gson)
    * view_name[_ACCEPT_VERSION].gson (Example: show_v1.0.gson)
    * view_name.gson (Example: show.gson)

The content type (defined by either the `ACCEPT` header or file extension in the URI) is taken into account to allow different formats for the same view.

Also you can for example define custom content types in `application.yml`:

    grails:
        mime:
            types:
                all:      "*/*"
                book:     "application/vnd.books.org.book+json"
                bookList: "application/vnd.books.org.booklist+json"

And then define a view such as `show.book.gson` for that particular type.

The version (defined by the `ACCEPT_VERSION` header) is taken into account to allow versioned APIs.

Finally, the locale allows different views for different languages.

So for example if the `CONTENT_TYPE` of the request is `application/json`

### HAL Support

HAL is a specification for linking resources in an API and makes an API explorable.

JSON views include HAL support. For example:

    model {
        Book book
    }
    json {
        hal.links(book)
        hal.embedded {
            author( book.authors.first() ) { Author author ->
                name author.name
            }
        }
        title book.title
    }


This produces:

    {
        "_links": {
            "self": {
                "href": "http://localhost:8080/book/show/1",
                "hreflang": "en",
                "type": "application/hal+json"
            }
        },
        "_embedded": {
            "author": {
                "_links": {
                    "self": {
                        "href": "http://localhost:8080/author/show/1",
                        "hreflang": "en",
                        "type": "application/hal+json"
                    }
                },
                "name": "Stephen King"
            }
        },
        "title": "The Stand"
    }

You can set the HAL content type to an explicit content type or one of the named content types defined in `grails.mime.types` in `application.yml`:


    model {
        Book book
    }
    hal.type("book")
    json {
        ...
    }

The above example sets the content type to `application/vnd.books.org.book+json`

### Changing the view base class

All JSON views subclass the [JsonViewTemplate](json/src/main/groovy/grails/plugin/json/view/JsonViewTemplate.groovy) class by default.

You can however change the subclass (which should be a subclass of `JsonViewTemplate`) using configuration:

    grails:
        views:
            json:
                compileStatic: true
                baseTemplateClass: com.example.MyCustomJsonViewTemplate

### Adding New Helper Methods via Traits

Alternatively, rather than modifying the base class, you can instead just add new methods via traits.

For example the [HttpView](core/src/main/groovy/grails/views/api/HttpView.groovy) uses the `Enhances` annotation to add the `page` object to all views:

    import grails.artefact.Enhances
    import grails.views.Views

    @Enhances(Views.TYPE)
    trait HttpView {

        /**
         * @return The page object
         */
        Page page
        ...
    }

The result is all JSON views have a `page` object that can be used to control the HTTP response:

    page.header "Token", "foo"


## Markup Views

Markup views use Groovy's [MarkupTemplateEngine](http://docs.groovy-lang.org/docs/next/html/documentation/template-engines.html#_the_markuptemplateengine).

### Installation

Add the following dependency to the `dependencies` block of your `build.gradle`:

    compile "org.grails.plugins:views-markup:1.0.0.M1"

To enable Gradle compilation of Markup views for production environment add the following to the `buildscript` `dependencies` block:

    buildscript {
        ...
        dependencies {
            ...
            classpath "org.grails.plugins:views-gradle:1.0.0.M1"
        }
    }

Then apply the `org.grails.plugins.views-markup` Gradle plugin after any Grails core gradle plugins:

    ...
    apply plugin: "org.grails.grails-web"
    apply plugin: "org.grails.plugins.views-markup"

This will add a `compileGmlViews` task to Gradle that will be executed when a WAR or JAR is built.

### Usage

Markup views go in the `grails-app/views` directory using the file extension `gml`. They are regular Groovy scripts and can be opened in the Groovy editor of your IDE.

Example Markup View:

    model {
        Iterable<Map> cars
    }
    xmlDeclaration()
    cars {
        cars.each {
            car(make: it.make, model: it.model)
        }
    }

This produces the following output given a model such as `[cars:[[make:"Audi", model:"A5"]]]`:

    <?xml version='1.0'?>
    <cars><car make='Audi' model='A5'/></cars>

For further examples see Groovy's [MarkupTemplateEngine](http://docs.groovy-lang.org/docs/next/html/documentation/template-engines.html#_the_markuptemplateengine) documentation.

All Markup views implement the [MarkupView](markup/src/main/groovy/grails/plugin/markup/view/api/MarkupView.groovy) and [HttpView](core/src/main/groovy/grails/views/api/HttpView.groovy) traits and extend from the [MarkupViewTemplate](markup/src/main/groovy/grails/plugin/markup/view/MarkupViewTemplate.groovy) base class.

### Configuration

Markup views configuration can be altered with `application.yml`. Any of the properties within the [ViewConfiguration](core/src/main/groovy/grails/views/ViewConfiguration.groovy) interface and Groovy's [TemplateConfiguration](http://docs.groovy-lang.org/latest/html/api/groovy/text/markup/TemplateConfiguration.html) class can be set. For example:

    grails:
        views:
            markup:
                compileStatic: true
                cacheTemplates: true
                autoIndent: true
                ...

Alternatively you can register a new `MarkupViewConfiguration` bean using the bean name `markupViewConfiguration` in `grails-app/conf/spring/resources.groovy`.

### Shared Features with JSON views

All of the features of JSON views (links, page header customization, i18n etc.) work the same in markup views.
