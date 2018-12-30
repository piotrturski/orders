package net.piotrturski.shop.order

import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import kotlin.IllegalArgumentException

@ControllerAdvice
internal class ExceptionMapper {


    @ExceptionHandler(IllegalArgumentException::class)
    fun `404`(exc: Exception) = ResponseEntity(
            mapOf("error" to exc.message),
            HttpHeaders().apply { this.contentType = MediaType.APPLICATION_JSON_UTF8 },
            HttpStatus.BAD_REQUEST)

}