package net.piotrturski.shop.order

import com.google.common.base.Preconditions
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal


@RestController
@RequestMapping("/products")
class ProductController(val productRepository: ProductRepository) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun insert(@RequestBody product: Product): Mono<Product> {
        Preconditions.checkArgument(product.id == null, "id must be null")
        return productRepository.insert(product)
    }

    @PutMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    fun update(@PathVariable id:String, @RequestBody product: Product): Mono<Void> {
        return productRepository.save(product).then()
    }

    @GetMapping
    fun get(): Flux<Product> {
        return productRepository.findAll()
    }

}

data class Product(var id: String? = null, var price: BigDecimal, var name:String)

interface ProductRepository: ReactiveMongoRepository<Product, String>