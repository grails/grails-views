package grails.plugin.json.view

import grails.plugin.json.builder.JsonOutput
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.view.api.JsonView
import grails.views.AbstractWritableScript
import grails.views.Views
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.buffer.FastStringWriter


/**
 * All JSON views subclass this superclass
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
@InheritConstructors
abstract class JsonViewTemplate extends AbstractWritableScript implements JsonView {
    public static final String EXTENSION = "gson"
    public static final String TYPE = "view.gson"


    Object root

    @Override
    Writer doWrite(Writer out) throws IOException {
        if(!prettyPrint) {
            this.json = new StreamingJsonBuilder(out)
            run()
            return out
        }
        else {
            def writer = new FastStringWriter()
            setOut(writer)
            this.json = new StreamingJsonBuilder(writer)
            run()
            def prettyOutput = JsonOutput.prettyPrint(writer.toString())
            out.write(prettyOutput)
            return out
        }
    }

    /**
     * TODO: When Groovy 2.4.5 go back to JsonBuilder from groovy-json
     *
     * @param callable
     * @return
     */
    StreamingJsonBuilder json(@DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure callable) {
        this.root = callable
        json.call callable
        return json
    }

    StreamingJsonBuilder json(Iterable iterable) {
        this.root = iterable
        json.call iterable.asList()
        return json
    }

    StreamingJsonBuilder json(Map map) {
        this.root = map
        json.call map
        return json
    }

    /**
     * Print unescaped json directly
     *
     * @param unescaped The unescaped JSON produced from templates
     *
     * @return The json builder
     */
    StreamingJsonBuilder json(JsonOutput.JsonUnescaped unescaped) {
        print(unescaped.text)
        return json
    }

    /**
     * TODO: When Groovy 2.4.5 go back to JsonBuilder from groovy-json
     *
     * @param callable
     * @return
     */
    StreamingJsonBuilder json(Iterable iterable, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure callable) {
        json.call(iterable.asList(), callable)
        return json
    }

    StreamingJsonBuilder json(Object...args) {
        if(args.length == 1) {
            def val = args[0]
            if(val instanceof JsonOutput.JsonUnescaped) {
                json((JsonOutput.JsonUnescaped)val)
            }
            else {
                json.call val
            }
        }
        else {
            json.call args
        }
        return json
    }


}
