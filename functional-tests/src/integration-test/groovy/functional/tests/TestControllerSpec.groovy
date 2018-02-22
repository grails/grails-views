package functional.tests

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Issue

@Integration
@Rollback
class TestControllerSpec extends GebSpec {

    @Issue('https://github.com/grails/grails-core/issues/10582')
    void 'test responding after an action triggered by a HTTP 401 response is possible'() {
        when: 'JSON is requested'
            URL url = new URL("${baseUrl}/test/triggerUnauthorized")
            HttpURLConnection conn = (HttpURLConnection)url.openConnection()

            String content = ""
            try {
                conn.connect()
                conn.getInputStream()
            } catch (IOException ignored) {
                content = new BufferedReader(new InputStreamReader((conn.getErrorStream()))).text
            }

        then: 'the JSON view is rendered'
            content == '{"message":"Unauthorized GSON"}'
    }
}
