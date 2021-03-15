package functional.tests

import grails.testing.mixin.integration.Integration
import grails.web.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

@Integration(applicationClass = Application)
class ProductSpec extends HttpClientSpec {

    void testEmptyProducts() {
        when:
        HttpRequest request = HttpRequest.GET("/products")
        HttpResponse<Map> resp = client.toBlocking().exchange(request, Map)

        then:
        resp.status == HttpStatus.OK
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).isPresent()
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).get() == 'application/hal+json;charset=UTF-8'

        and: "The values returned are there"
        resp.body().count == 0
        resp.body().max == 10
        resp.body().offset == 0
        resp.body().sort == null
        resp.body().order == null
        and: "the hal _links attribute is present"
        resp.body()._links.size() == 1
        resp.body()._links.self.href.startsWith("${baseUrl}/product")

        and: "there are no products yet"
        resp.body()._embedded.products.size() == 0
    }

    void testSingleProduct() {
        given:
        HttpResponse<Map> createResp = client.toBlocking()
                .exchange(
                        HttpRequest.POST("/products", [name: "Product 1",
                description: "product 1 description",
                price: 123.45]), Map)
        assert createResp.status == HttpStatus.CREATED

        when: "We get the products"
        HttpRequest request = HttpRequest.GET("/products")
        HttpResponse<Map> resp = client.toBlocking().exchange(request, Map)

        then:
        resp.status == HttpStatus.OK
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).isPresent()
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).get() == 'application/hal+json;charset=UTF-8'

        and: "The values returned are there"
        resp.body().count == 1
        resp.body().max == 10
        resp.body().offset == 0
        resp.body().sort == null
        resp.body().order == null
        and: "the hal _links attribute is present"
        resp.body()._links.size() == 1
        resp.body()._links.self.href.startsWith("${baseUrl}/product")

        and: "the product is present"
        resp.body()._embedded.products.size() == 1
        resp.body()._embedded.products.first().name == "Product 1"

        cleanup:
        resp = client.toBlocking().exchange(HttpRequest.DELETE("/products/${createResp.body().id}"))
        assert resp.status() == HttpStatus.OK
    }

    void "test a page worth of products"() {
        given:
        def productsIds = []
        15.times { productNumber ->
            ProductVM product = new ProductVM(name: "Product $productNumber",
                           description: "product ${productNumber} description",
                           price: productNumber + (productNumber / 100))
            HttpResponse<Map> createResp = client.toBlocking()
                    .exchange(HttpRequest.POST("/products", product), Map)
            assert createResp.status == HttpStatus.CREATED
            productsIds << createResp.body().id
        }

        when: "We get the products"
        HttpRequest request = HttpRequest.GET("/products")
        HttpResponse<Map> resp = client.toBlocking().exchange(request, Map)
        def json = resp.body()
        then:
        resp.status == HttpStatus.OK
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).isPresent()
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).get() == 'application/hal+json;charset=UTF-8'

        and: "The values returned are there"
        json.count == 15
        json.max == 10
        json.offset == 0
        json.sort == null
        json.order == null
        and: "the hal _links attribute is present"
        json._links.size() == 4
        json._links.self.href.startsWith("${baseUrl}/product")
        json._links.first.href.startsWith("${baseUrl}/product")
        json._links.next.href.startsWith("${baseUrl}/product")
        json._links.last.href.startsWith("${baseUrl}/product")

        and: "the product is present"
        json._embedded.products.size() == 10

        cleanup:
        productsIds.each { id ->
            resp = client.toBlocking().exchange(HttpRequest.DELETE("/products/${id}"))
            assert resp.status() == HttpStatus.OK
        }
    }

    void "test a middle page worth of products"() {
        given:
        def productsIds = []
        30.times { productNumber ->
            ProductVM product = new ProductVM(name: "Product $productNumber",
                           description: "product ${productNumber} description",
                           price: productNumber + (productNumber / 100))
            HttpResponse<Map> createResp = client.toBlocking().exchange(HttpRequest.POST("/products", product), Map)
            assert createResp.status == HttpStatus.CREATED
            productsIds << createResp.body().id
        }

        when: "We get the products"
        HttpRequest request = HttpRequest.GET("/products?offset=10")
        HttpResponse<Map> resp = client.toBlocking().exchange(request, Map)

        then:
        resp.status == HttpStatus.OK
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).isPresent()
        resp.headers.getFirst(HttpHeaders.CONTENT_TYPE).get() == 'application/hal+json;charset=UTF-8'

        and: "The values returned are there"
        resp.body().count == 30
        resp.body().max == 10
        resp.body().offset == 10
        resp.body().sort == null
        resp.body().order == null
        and: "the hal _links attribute is present"
        resp.body()._links.size() == 5
        resp.body()._links.self.href.startsWith("${baseUrl}/product")
        resp.body()._links.first.href.startsWith("${baseUrl}/product")
        resp.body()._links.prev.href.startsWith("${baseUrl}/product")
        resp.body()._links.next.href.startsWith("${baseUrl}/product")
        resp.body()._links.last.href.startsWith("${baseUrl}/product")

        and: "the product is present"
        resp.body()._embedded.products.size() == 10

        cleanup:
        productsIds.each { id ->
            resp = client.toBlocking().exchange(HttpRequest.DELETE("/products/${id}"))
            assert resp.status() == HttpStatus.OK
        }
    }
}

class ProductVM {
    String name
    String description
    BigDecimal price
}
