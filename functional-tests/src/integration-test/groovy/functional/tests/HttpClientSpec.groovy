package functional.tests

import grails.testing.spock.OnceBefore
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.HttpClientConfiguration
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration

class HttpClientSpec extends Specification {

    @Shared
    @AutoCleanup
    HttpClient client

    @Shared
    String baseUrl

    @OnceBefore
    void init() {
        this.baseUrl = "http://localhost:$serverPort"
        DefaultHttpClientConfiguration configuration = new DefaultHttpClientConfiguration()
        configuration.setReadTimeout(Duration.ofMinutes(5))
        this.client  = new DefaultHttpClient(new URL(baseUrl), configuration)
    }
}
