package net.piotrturski.shop

import net.piotrturski.shop.order.OrderRepository
import net.piotrturski.shop.order.Product
import net.piotrturski.shop.order.ProductRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import reactor.core.publisher.Mono

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class SpringBeanMockExample {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var orderRepository: OrderRepository
    @MockBean
    lateinit var productRepository: ProductRepository

    @Test
    fun `should update product`() {

        val product = Product(id = "7abc", price = 12.9.toBigDecimal(), name = "different name")
        BDDMockito.given(productRepository.save(product)).willReturn(Mono.just(product))

        mockMvc.async(MockMvcRequestBuilders.put("/products/7abc").jsonContent("""
            {
                "price":12.9,
                "name":"different name"
            }
        """)
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

}