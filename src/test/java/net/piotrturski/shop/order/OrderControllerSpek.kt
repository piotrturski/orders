package net.piotrturski.shop.order

import net.piotrturski.shop.exchange
import net.piotrturski.shop.jsonContent
import net.piotrturski.shop.restMockMvc
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


class OrderControllerSpek: Spek({

    val productRepository = mock(ProductRepository::class.java)
    val orderRepository = mock(OrderRepository::class.java)
    val clock = Clock.fixed(Instant.parse("2018-05-01T14:50:00.00Z"), ZoneOffset.UTC)
    val mockMvc = restMockMvc(OrderController(productRepository, orderRepository, clock))

    it("should create order") {

        val products = listOf(
                Product("123", 1.23.toBigDecimal(), "product 1"),
                Product("456", 12.toBigDecimal(), "product 2"))

        given(productRepository.findAllById(setOf("123", "456"))).willReturn(products.toFlux())

        val order = Order(null, "abc@def.com", products, LocalDateTime.now(clock))
        given(orderRepository.save(order)).willReturn(Mono.just(order.copy(id="order-id")))

        mockMvc.exchange(post("/orders").jsonContent(
                """{"email":"abc@def.com", "productIds":["123","456"]}"""))
                .andExpect(status().isCreated)
                .andExpect(content().json("""{
                    "id":"order-id", "email":"abc@def.com", "date":"2018-05-01T14:50:00",
                    "products":[
                            {"id":"123", "price":1.23, "name":"product 1"},
                            {"id":"456", "price":12, "name":"product 2"}
                               ]
                }"""))
    }

    it("should reject invalid input") {

        val bigJsonArray = IntRange(1, 101).map { """"$it"""" }.joinToString(prefix = "[", postfix = "]")

        mockMvc.exchange(post("/orders").jsonContent(
                """{"email":"abc@d.", "productIds":$bigJsonArray}"""
        ))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(content().json("""
                    [
                        {"field":"productIds","error":"size must be between 1 and 100"},
                        {"field":"email","error":"must be a well-formed email address"}
                    ]"""))
    }

    it("should return 4xx on non-existing product id") {

        given(productRepository.findAllById(setOf("1", "2"))).willReturn(
                setOf(Product("1", 2.toBigDecimal(), "name")).toFlux())

        mockMvc.exchange(post("/orders").jsonContent(
                """{"email":"abc@def.com", "productIds":["1","2"]}"""))
                .andExpect(status().isBadRequest)
                .andExpect(content().json("""{"error":"illegal product id"}"""))
    }

})