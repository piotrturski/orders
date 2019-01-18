package net.piotrturski.shop.product

import net.piotrturski.shop.order.Product
import net.piotrturski.shop.order.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.util.function.Consumer


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductITest {

    @Autowired lateinit var webClient: WebTestClient
    @Autowired lateinit var productRepository: ProductRepository

    @Before
    fun setUp() {
       productRepository.deleteAll().block()
    }

    @Test
    fun `should return 4xx on bad product details`() {
        webClient.post().uri("/products")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .syncBody("""
            {
                "price":-3,
                "name":"dsd"
            }
        """).exchange().expectStatus().is4xxClientError
                .expectBody().consumeWith { println(it.responseBody.toString()) }
    }


    @Test
    fun `should create, get and update products`() {
        webClient.post().uri("/products")
                .body(Mono.just(Product(null, BigDecimal.ONE, "abc")), Product::class.java)
                .exchange()
                .expectStatus().isCreated
                .expectBody().jsonPath("id").isNotEmpty

        var id: String = "";

        webClient.get().uri("/products")
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath(".id").isNotEmpty
                .jsonPath(".id").value(Consumer<List<String>> { it ->
                    assertThat(it).hasSize(1)
                    id = it[0]
                })

        webClient.put().uri("/products/$id")
                .body(Mono.just(Product(id, BigDecimal.TEN, "changed-name")), Product::class.java)
                .exchange()
                .expectStatus().isNoContent
                .expectBody().isEmpty


        webClient.get().uri("/products")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                    .jsonPath(".id").value(Consumer<List<String>> { it ->
                        assertThat(it).hasSize(1)
                    })
                    .jsonPath("[0].id").isEqualTo(id)
                    .jsonPath("[0].price").isEqualTo(10)
                    .jsonPath("[0].name").isEqualTo("changed-name")
    }
}

