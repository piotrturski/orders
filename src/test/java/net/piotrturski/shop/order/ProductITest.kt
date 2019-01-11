package net.piotrturski.shop.order

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import reactor.core.publisher.Flux
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

@RunWith(SpringRunner::class)
//@WebMvcTest
//@SpringBootTest
@SpringBootTest
@AutoConfigureMockMvc
class ProductMvc {

    @Autowired lateinit var mockMvc: MockMvc

    @MockBean lateinit var orderRepository: OrderRepository
    @MockBean lateinit var productRepository: ProductRepository

//    @Before
//    fun before {
//        mockMvc.
//    }

    @Test
    fun `should return all products`() {


        given(productRepository.findAll()).willReturn(Flux.just(
                Product(
                    id = "1",
                    price = 17.45.toBigDecimal(),
                    name = "product 1"),

                Product(
                    id = "2",
                    price = 11.toBigDecimal(),
                    name = "product 2"
                )
                ))

        //TODO switch to SSE?
        mockMvc.exchange(get("/products"))
                .andExpect(status().isOk)
                .andExpect(content().json("""
                            [
                                {"id":"1", "price":17.45, "name":"product 1"},
                                {"id":"2", "price":11, "name":"product 2"}
                            ]
                    """))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_UTF8))
    }

    @Test
    fun `should create order`() {

        val product = Product(id = null, price = 11.34.toBigDecimal(), name = "a product")
        given(productRepository.insert(product))
                .willReturn(Mono.just(product.copy(id="123")))

        mockMvc.exchange(post("/products").jsonContent("""
           {
                "price":11.34,
                "name":"a product"
           }
        """)
        )
                .andExpect(status().isCreated)
                .andExpect(content().json("""
                   {
                        "id":"123",
                        "price":11.34,
                        "name":"a product"
                   }
                """))

    }

    @Test
    fun `should update product`() {

        val product = Product(id = "7abc", price = 12.9.toBigDecimal(), name = "different name")
        given(productRepository.save(product)).willReturn(Mono.just(product))

        mockMvc.exchange(put("/products/7abc").jsonContent("""
            {
                "price":12.9,
                "name":"different name"
            }
        """)
        )
//                .andReturn().run { mockMvc.perform(asyncDispatch(this)) }
                .andExpect(status().isNoContent)
    }

    @Test
    fun `should return client error on bad input`() {
        val result = mockMvc.perform(post("/products").jsonContent("""
            {
                "price":-3,
                "name":"dsd"
            }
        """)
        )
//                .andExpect(status().is4xxClientError)
//                .andDo(print())
                .andReturn()

        println("*********************************88")
        println(result.response.status)


    }
}

class ValidationSpek: Spek({

    describe("product controller") {

        val productRepository: ProductRepository = mock(ProductRepository::class.java)

        val productController = ProductController(productRepository)
        val mockMvc = restMockMvc(productController)


        listOf(
//            """{"price": -3, "name":"dsd"}""", "price", "must be greater than 1",
            """{"price": -3, "name":""}""",
            """{"price": -3, "name":"", "sdfs":"sdfs"}""",
            """{"price": -3, "name":" "}"""//, "name", "must not be blank"
//            """{"price": null, "name":""}""", "name", "must not be blank",
//            """{"price": -3, "name":""}""", "name", "must not be blank1"
        )
            .forEach {

                it("should return 4xx for json '$it'") {

                    mockMvc.perform(post("/products").jsonContent(it))
                            .andExpect(status().isUnprocessableEntity)
                            .andExpect(content().json("""
                                [
                                    {"field":"price","error":"must be greater than 0"},
                                    {"field":"name","error":"must not be blank"}
                                ]"""))
                }
            }

        listOf(
                """{"price": null, "name":"a"}""",
                """{"pric""",
                """{"price": 3, "name":null}"""
        )
                .forEach {

                    it("should return 4xx for json: $it") {

                        mockMvc.perform(post("/products").jsonContent(it))
                                .andExpect(status().isBadRequest)
                    }

                }


//        """[{"field":"price","error":"must be greater than 0"},{"field":"name","error":"must not be blank"}]"""
    }

})

