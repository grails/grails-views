package grails.plugins.json.view

import grails.plugins.json.builder.JsonOutput
import grails.plugins.json.builder.StreamingJsonBuilder
import grails.views.AbstractWritableScript
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.buffer.FastStringWriter


/**
 * All JSON views subclass this superclass
 *
 *  TODO:
 *      - declaring templates for types
 *      - HAL integration
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
@InheritConstructors
abstract class JsonTemplate extends AbstractWritableScript {

    public static final String EXTENSION = "gson"

    StreamingJsonBuilder json
    boolean prettyPrint = false
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
        json.call args
        return json
    }


}
