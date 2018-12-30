package net.piotrturski.shop.order

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.google.common.base.Preconditions
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.BAD_REQUEST
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
import com.google.common.collect.ImmutableMap
import org.springframework.validation.BindingResultUtils.getBindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.annotation.Validated
import java.lang.Exception
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive


@RestController
@RequestMapping("/products")
//@Validated
class ProductController(val productRepository: ProductRepository) {

    @PostMapping
    @ResponseStatus(CREATED)
//    @Validated
    fun insert(
            @Valid
            @RequestBody product: ProductBody): Mono<Product> {
        return productRepository.insert(product.toProductWithId(null))
    }

    @PutMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    fun update(@PathVariable id:String, @Valid @RequestBody product: ProductBody): Mono<Void> {
        return productRepository.save(product.toProductWithId(id)).then()
    }

    @GetMapping
    fun get(): Flux<Product> {
        return productRepository.findAll()
    }

    @ExceptionHandler
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    fun validationError(e: MethodArgumentNotValidException): Any {
        return e.bindingResult.fieldErrors.stream()
                .map { err -> ImmutableMap.of("field", err.field, "error", err.defaultMessage!!) }
                .toArray()
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    fun parsingError(e: HttpMessageNotReadableException): Any {
        return ImmutableMap.of<String, Any>("sample message", "{}")
    }

//    @ExceptionHandler
//    @ResponseStatus(INTERNAL_SERVER_ERROR)
//    fun a(exc: Exception) {}

}

data class Product(val id: String?, val price: BigDecimal, val name:String) {
    init {
        Preconditions.checkArgument(price > JavaSrc.ZERO, "price must be positive")
        Preconditions.checkArgument(name.isNotBlank(), "name must not be blank")
    }
}

//@Validated
data class ProductBody(@field:Positive val price: BigDecimal, @field:NotBlank val name:String) {

    fun toProductWithId(id: String?) = Product(id = id, price = price, name = name)
}

data class ProductContainer(val id: String, @JsonUnwrapped val body: ProductBody)

interface ProductRepository: ReactiveMongoRepository<Product, String>