package grails.plugin.json.view.test

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.springframework.http.HttpStatus

/**
 * Created by graemerocher on 15/04/2016.
 */
@CompileStatic
@Canonical
class JsonRenderResult {
    HttpStatus status = HttpStatus.OK
    String message = HttpStatus.OK.getReasonPhrase()
    String contentType
    Map<String, String> headers = [:]
    Object json
    String jsonText
}
