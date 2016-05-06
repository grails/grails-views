package functional.tests

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.web.http.HttpHeaders

@Integration
@Rollback
class ProductSpec extends GebSpec {

    def setup() {
    }

    def cleanup() {
    }

    void testEmptyProducts() {
        given:
            def builder = new RestBuilder()

        when:
            def resp = builder.get("$baseUrl/products")

        then:
            resp.status == 200
            resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/hal+json;charset=UTF-8'

        and: "The values returned are there"
            resp.json.count == 0
            resp.json.max == 10
            resp.json.offset == 0
            resp.json.sort == null
            resp.json.order == null
        and: "the hal _links attribute is present"
            resp.json._links.size() == 1
            resp.json._links.self.href.startsWith("$baseUrl/product")

        and: "there are no products yet"
            resp.json._embedded.products.size() == 0
    }

    void testSingleProduct() {
        given:
            def builder = new RestBuilder()

            def createResp = builder.post("$baseUrl/products") {
                json {
                    name = "Product 1"
                    description = "product 1 description"
                    price = 123.45
                }
            }
            assert createResp.status == 201

        when: "We get the products"
            def resp = builder.get("$baseUrl/products")

        then:
            resp.status == 200
            resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/hal+json;charset=UTF-8'

        and: "The values returned are there"
            resp.json.count == 1
            resp.json.max == 10
            resp.json.offset == 0
            resp.json.sort == null
            resp.json.order == null
        and: "the hal _links attribute is present"
            resp.json._links.size() == 1
            resp.json._links.self.href.startsWith("$baseUrl/product")

        and: "the product is present"
            resp.json._embedded.products.size() == 1
            resp.json._embedded.products.first().name == "Product 1"

        cleanup:
            def delResp = builder.delete("$baseUrl/products/${createResp.json.id}")
            assert delResp.status == 204
    }

    void "test a page worth of products"() {
        given:
            def builder = new RestBuilder()
            def productsIds = []
            15.times { productNumber ->
                def createResp = builder.post("$baseUrl/products") {
                    json {
                        name = "Product $productNumber"
                        description = "product ${productNumber} description"
                        price = productNumber + (productNumber/100)
                    }
                }
                assert createResp.status == 201
                productsIds << createResp.json.id
            }


        when: "We get the products"
            def resp = builder.get("$baseUrl/products")

        then:
            resp.status == 200
            resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/hal+json;charset=UTF-8'

        and: "The values returned are there"
            resp.json.count == 15
            resp.json.max == 10
            resp.json.offset == 0
            resp.json.sort == null
            resp.json.order == null
        and: "the hal _links attribute is present"
            resp.json._links.size() == 4
            resp.json._links.self.href.startsWith("$baseUrl/product")
            resp.json._links.first.href.startsWith("$baseUrl/product")
            resp.json._links.next.href.startsWith("$baseUrl/product")
            resp.json._links.last.href.startsWith("$baseUrl/product")

        and: "the product is present"
            resp.json._embedded.products.size() == 10

        cleanup:
            productsIds.each{id ->
                def delResp = builder.delete("$baseUrl/products/${id}")
                assert delResp.status == 204
            }
    }

    void "test a middle page worth of products"() {
        given:
            def builder = new RestBuilder()
            def productsIds = []
            30.times { productNumber ->
                def createResp = builder.post("$baseUrl/products") {
                    json {
                        name = "Product $productNumber"
                        description = "product ${productNumber} description"
                        price = productNumber + (productNumber/100)
                    }
                }
                assert createResp.status == 201
                productsIds << createResp.json.id
            }


        when: "We get the products"
            def resp = builder.get("$baseUrl/products?offset=10")

        then:
            resp.status == 200
            resp.headers.getFirst(HttpHeaders.CONTENT_TYPE) == 'application/hal+json;charset=UTF-8'

        and: "The values returned are there"
            resp.json.count == 30
            resp.json.max == 10
            resp.json.offset == 10
            resp.json.sort == null
            resp.json.order == null
        and: "the hal _links attribute is present"
            resp.json._links.size() == 5
            resp.json._links.self.href.startsWith("$baseUrl/product")
            resp.json._links.first.href.startsWith("$baseUrl/product")
            resp.json._links.prev.href.startsWith("$baseUrl/product")
            resp.json._links.next.href.startsWith("$baseUrl/product")
            resp.json._links.last.href.startsWith("$baseUrl/product")

        and: "the product is present"
            resp.json._embedded.products.size() == 10

        cleanup:
            productsIds.each{id ->
                def delResp = builder.delete("$baseUrl/products/${id}")
                assert delResp.status == 204
            }
    }
}
