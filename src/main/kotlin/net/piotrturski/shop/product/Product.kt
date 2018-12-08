package net.piotrturski.shop.product

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.math.BigDecimal


@RestController
@RequestMapping("/product")
class ProductController(val productRepository: ProductRepository) {

    @PostMapping
    fun save(): Mono<Void> {
        return Mono.empty()
    }

    @PutMapping
    fun update(): Mono<Void> {
        return Mono.empty()
    }

    @GetMapping("/{:id}")
    fun get(@PathVariable id:String): Mono<Product> {
        return productRepository.findById(id)
//        return Mono.just(Product())
    }

}

data class Product(var id: String? = null, var price: BigDecimal, var name:String)

interface ProductRepository :
        ReactiveMongoRepository
//        ReactiveCrudRepository
<Product, String>