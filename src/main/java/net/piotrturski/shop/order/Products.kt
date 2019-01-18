package net.piotrturski.shop.order

import com.fasterxml.jackson.annotation.JsonUnwrapped
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
import net.piotrturski.infra.JavaInterop
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

@RestController
@RequestMapping("/products")
//@Validated
class ProductController(val productRepository: ProductRepository) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun insert(@Valid @RequestBody product: ProductBody): Mono<Product> =
            productRepository.insert(product.toProductWithId(null))

    @PutMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    fun update(@PathVariable id:String, @Valid @RequestBody product: ProductBody): Mono<Void> =
            productRepository.save(product.toProductWithId(id)).then()

    @GetMapping
    fun get(): Flux<Product> = productRepository.findAll()

}

data class Product(val id: String?, val price: BigDecimal, val name:String) {
    init {
        Preconditions.checkArgument(price > BigDecimal.ZERO, "price must be positive")
        Preconditions.checkArgument(name.isNotBlank(), "name must not be blank")
    }
}

//@Validated
data class ProductBody(@field:Positive val price: BigDecimal, @field:NotBlank val name:String) {

    fun toProductWithId(id: String?) = Product(id = id, price = price, name = name)
}

interface ProductRepository: ReactiveMongoRepository<Product, String>