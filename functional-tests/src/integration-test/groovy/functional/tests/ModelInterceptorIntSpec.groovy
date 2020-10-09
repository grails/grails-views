package functional.tests

import grails.testing.mixin.integration.Integration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

@Integration(applicationClass = Application)
class ModelInterceptorIntSpec extends HttpClientSpec {

    @Autowired
    ModelInterceptor modelInterceptor

    @Unroll
    void "interceptor should get model from #controller"() {
        given:
        def request = HttpRequest.GET("/$controller")
                .contentType(MediaType.APPLICATION_JSON)
        HttpResponse response = client.toBlocking().exchange(request, Map)

        expect:
        response.status == HttpStatus.OK
        modelInterceptor.latestModel != null

        where:
        controller << ["modelAndView",
                       "respond",
                       "return"]
    }
}
