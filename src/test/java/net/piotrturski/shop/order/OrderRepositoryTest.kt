package net.piotrturski.shop.order

import net.piotrturski.shop.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.Duration
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@DataMongoTest
class OrderRepositoryTest {

    @Autowired lateinit var orderRepository: OrderRepository

    val date = LocalDateTime.parse("2018-05-20T15:41:43.003")

    @Before
    fun setup() {
        orderRepository.deleteAll().block()
    }

    @Test
    fun `should find orders between dates`() {

        val orders = IntRange(1, 6)
                .map {date + Duration.ofMillis(it.toLong())}
                .map {Order(date = it, email = "a@b.com", products = emptyList())}
                .map { orderRepository.save(it).block()!! }

        orders.forEach { println(it.date) }

        println(orderRepository.count().block()!!)

        assertThat(
                orderRepository.findByDateBetween(orders[1].date, orders[4].date)
        )
                .containsExactlyInAnyOrder(orders[2], orders[3])
    }

    @Test
    fun `should fail on non-existing product id`() {
        orderRepository
    }
}
