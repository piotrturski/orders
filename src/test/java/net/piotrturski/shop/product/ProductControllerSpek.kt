package net.piotrturski.shop.product

import net.piotrturski.shop.order.Product
import net.piotrturski.shop.order.ProductController
import net.piotrturski.shop.order.ProductRepository
import net.piotrturski.shop.exchange
import net.piotrturski.shop.forEachCrossProduct
import net.piotrturski.shop.jsonContent
import net.piotrturski.shop.restMockMvc
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import reactor.core.publisher.toMono
import java.math.BigDecimal

class ProductControllerSpek: Spek({

    describe("product controller") {

        val productRepository = mock(ProductRepository::class.java)
        val mockMvc = restMockMvc(ProductController(productRepository))

        group("should reject bad product description") {

            forEachCrossProduct(
                    listOf(
                            """{"price": -3, "name":""}""",
                            """{"price": -3, "name":"", "extra-field":"whatever"}""",
                            """{"price": -3, "name":" "}"""),
                    listOf(
                            post("/products"),
                            put("/products/abc"))

            ) { json, request ->

                it("should return 4xx when ${request.prettify()} with content $json") {

                    mockMvc.exchange(request.jsonContent(json))
                            .andExpect(status().isUnprocessableEntity)
                            .andExpect(content().json("""
                                    [
                                        {"field":"price","error":"must be greater than 0"},
                                        {"field":"name","error":"must not be blank"}
                                    ]"""))
                }
            }

            forEachCrossProduct(
                    listOf(
                            """{"price": null, "name":"a"}""",
                            """{"pric""",
                            """{"price": 3, "name":null}"""),
                    listOf(
                            post("/products"),
                            put("/products/abc"))

            ) { json, request ->

                it("should return 4xx when ${request.prettify()} with content $json") {

                    mockMvc.exchange(post("/products").jsonContent(json))
                            .andExpect(status().isBadRequest)
                }
            }
        }

        group("should accept correct product") {

            val product = Product(id = null, price = BigDecimal.TEN, name = "a name");

            it("should insert correct product") {

                given(productRepository.insert(product)).willReturn(product.copy(id = "some id").toMono())

                mockMvc.exchange(post("/products")
                        .jsonContent("""{"price": 10, "name":"a name"}"""))
                        .andExpect(status().isCreated)
                        .andExpect(content().json(
                                """{"id":"some id", "price": 10, "name":"a name"}""", true))
            }

            it("should update correct product") {

                reset(productRepository)

                given(productRepository.save(product.copy(id = "abc"))).willReturn(product.toMono())

                mockMvc.exchange(put("/products/abc")
                            .jsonContent("""{"price": 10, "name":"a name"}"""))
                        .andExpect(status().isNoContent)
                        .andExpect(content().string(""))

            }
        }
    }

})

private fun MockHttpServletRequestBuilder.prettify(): String {
    fun field(fieldName: String) = ReflectionTestUtils.getField(this, fieldName)

    return "[${field("method")} to ${field("url")}]"
}