package net.piotrturski.shop.product

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal
import kotlin.streams.toList

@RunWith(SpringRunner::class)
@SpringBootTest
class ShopApplicationTests {

	@Autowired lateinit var productRepository: ProductRepository

	@Test
	fun contextLoads() {
		assertThat(productRepository).isNotNull

		val saved = productRepository.save(Product(null, BigDecimal.ONE, "ala")).block()

		println("saved: $saved")

		val products = productRepository.findAll()
				.log()
				.toStream()
				.toList()

		println("products: $products")
	}

}
