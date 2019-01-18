package net.piotrturski.shop.order

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.math.BigDecimal.TEN
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderITest {

    @Autowired lateinit var webClient: WebTestClient
    @Autowired lateinit var productRepository: ProductRepository
    @Autowired lateinit var orderRepository: OrderRepository
    @MockBean lateinit var clock: Clock

    @Before
    fun setUp() {
        productRepository.deleteAll().block()
        orderRepository.deleteAll().block()
        given(clock.zone).willReturn(ZoneOffset.UTC.normalized())
    }


    @Test
    fun `should save and get orders`() {
        given(clock.instant()).willReturn(Instant.parse("2018-11-16T14:43:00Z"))

        productRepository.saveAll(listOf(
                Product(null, TEN, "product 1"),
                Product(null, TEN, "product 2")
        )).blockLast()

        val ids = productRepository.findAll().map { it.id!! }.buffer().toMono().block()!!

        productRepository.save(Product(null, 17.toBigDecimal(), "product 3")).block()

        var firstOrderId: String

        webClient.post().uri("/orders")
                .body(Mono.just(CreateOrderRequest("my@email.com", ids)), CreateOrderRequest::class.java)
                .exchange()
                .expectStatus().isCreated
                .expectBodyList(Order::class.java)
                .value<WebTestClient.ListBodySpec<Order>>{ it ->

                    assertThat(it).hasSize(1)

                    assertThat(it.map { it.email }).containsExactly("my@email.com")

                    assertThat(it[0].products.map { it.name }).containsExactly("product 1", "product 2")
                    assertThat(it[0].totalPrice()).isEqualTo(20.toBigDecimal())
                    firstOrderId = it[0].id!!
                }


        given(clock.instant()).willReturn(Instant.parse("2018-11-20T14:43:00Z"))

        webClient.post().uri("/orders")
                .body(Mono.just(CreateOrderRequest("other@email.com", ids)), CreateOrderRequest::class.java)
                .exchange()
                .expectStatus().isCreated

        webClient.get().uri("/orders?start=2018-11-16T14:42&end=2018-11-20T14:43")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Order::class.java)
                .value<WebTestClient.ListBodySpec<Order>> { it ->

                    assertThat(it).hasSize(1)
                    assertThat(it[0].email).isEqualTo("my@email.com")
                }

    }

}