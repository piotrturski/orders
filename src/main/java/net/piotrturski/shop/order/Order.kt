package net.piotrturski.shop.order

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModelProperty
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus.CREATED
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size


data class Order(
        val id: String? = null,
        val email:String,
        val products: List<Product>,
//        @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") //not needed anymore
        val date: LocalDateTime) {

    fun totalPrice() = products.map { it.price }.fold(BigDecimal.ZERO, BigDecimal::plus)
}

data class CreateOrderRequest(
        @field:Email val email:String,
        @field:Size(min=1, max=100) val productIds: List<String>)

//@Service
//class OrderService(val productRepository: ProductRepository, val orderRepository: OrderRepository, val clock: Clock) {
//
//    fun makeOrder(@RequestBody orderRequest: CreateOrderRequest): Mono<Order> {
//        return productRepository.findAllById(orderRequest.productIds)
//                .buffer().toMono()
//                .map { Order(null, orderRequest.email, it, LocalDateTime.now(clock)) }
//                .flatMap{orderRepository.save(it)}
//    }
//}

@RestController
@RequestMapping("/orders")
class OrderController(val productRepository: ProductRepository, val orderRepository: OrderRepository, val clock: Clock) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun makeOrder(@RequestBody orderRequest: CreateOrderRequest): Mono<Order> {
        return productRepository.findAllById(orderRequest.productIds)
                .buffer().toMono()
                .map { Order(null, orderRequest.email, it, LocalDateTime.now(clock)) }
                .flatMap{orderRepository.save(it)}
    }

    @ApiResponses(value = arrayOf(
        ApiResponse(responseContainer = "List", response = Order::class, code = 200, message = "")
    ))
    @GetMapping
    fun get(// TODO change it globally?
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: LocalDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: LocalDateTime): Flux<Order> {
        return orderRepository.findByDateBetween(start, end)
    }
}

interface OrderRepository: ReactiveMongoRepository<Order, String> {
    fun findByDateBetween(start: LocalDateTime, end: LocalDateTime): Flux<Order>
}

