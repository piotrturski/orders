package net.piotrturski.shop.product

import net.piotrturski.shop.order.ProductBody
import net.piotrturski.shop.order.ProductController
import net.piotrturski.shop.order.ProductRepository
import net.piotrturski.shop.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal

@RunWith(SpringRunner::class)
@DataMongoTest
class ProductRepositoryTest {

    @Autowired
    lateinit var productRepository: ProductRepository

    lateinit var productController: ProductController

//    @Autowired
//    lateinit var template: ReactiveMongoTemplate

    val aBody = ProductBody(BigDecimal.TEN, "a name")
    val otherBody = ProductBody(price = BigDecimal.ONE, name = "other")

    @Before
    fun setup() {
        productRepository.deleteAll().block()
        productController = ProductController(productRepository)
    }

    //TODO can use full template to use update instead of upsert
    @Test
    fun `should save product`() {

        val product = productController.insert(aBody).block()

        assertThat(product?.id).isNotNull()

        assertThat(productController.get())
                .containsExactly(aBody.toProductWithId(product?.id))
    }


    @Test
    fun `should update product`() {
        val id = productController.insert(aBody).block()?.id!!

        productController.update(id, otherBody).block()

        assertThat(productController.get())
                .containsExactly(otherBody.toProductWithId(id))
    }

    @Test
    fun `should get all products`() {

        val saved = listOf(aBody, otherBody).associateBy {
            productController.insert(it).block()?.id!!
        }.map { (id, body) -> body.toProductWithId(id) }


        assertThat(productController.get())
                .containsAll(saved)
                .hasSameSizeAs(saved)
    }
}
