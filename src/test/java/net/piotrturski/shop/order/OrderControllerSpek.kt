package net.piotrturski.shop.order

import net.piotrturski.shop.exchange
import net.piotrturski.shop.jsonContent
import net.piotrturski.shop.restMockMvc
import org.aspectj.weaver.ast.Or
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import reactor.core.publisher.Flux
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

        given(productRepository.findAllById(listOf("123", "456"))).willReturn(products.toFlux())
//        given(clock.)
        val order = Order(null, "abc@def.com", products, LocalDateTime.now(clock))
        given(orderRepository.save(order)).willReturn(Mono.just(order.copy(id="order-id")))

        mockMvc.exchange(post("/orders").jsonContent("""{"email":"abc@def.com", "productIds":["123","456"]}"""))
//        mockMvc.exchange(post("/orders").jsonContent("""{"email":"abc@def.com"}"""))
                .andExpect(status().isCreated)
                // TODO check date "date":"2018-05-01T14:50",
                .andExpect(content().json("""{
                    "id":"order-id", "email":"abc@def.com",
                    "products":[
                            {"id":"123", "price":1.23, "name":"product 1"},
                            {"id":"456", "price":12, "name":"product 2"}
                               ]
                }"""))
    }

    group("should reject bad input") {



    }

})