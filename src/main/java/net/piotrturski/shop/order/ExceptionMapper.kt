package net.piotrturski.shop.order

import com.google.common.collect.ImmutableMap
import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import kotlin.IllegalArgumentException

@ControllerAdvice
@ResponseBody
internal class ExceptionMapper {


    @ExceptionHandler(IllegalArgumentException::class)
    fun `400`(exc: Exception) = ResponseEntity(
            mapOf("error" to exc.message),
            HttpHeaders().apply { this.contentType = MediaType.APPLICATION_JSON_UTF8 },
            HttpStatus.BAD_REQUEST)


    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun validationError(e: MethodArgumentNotValidException): Any {
        return e.bindingResult.fieldErrors.stream()
                .map { err -> ImmutableMap.of("field", err.field, "error", err.defaultMessage!!) }
                .toArray()
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun parsingError(e: HttpMessageNotReadableException): Any {
//        val a = "missing (therefore NULL) value for creator parameter \\S+ which is a non-nullable type".toRegex()
//        val matchResult = a.find(e.message.orEmpty())
//
//        matchResult
//
//        if (a.containsMatchIn(e.message ?: "")) {
//
//            e.message!!.en
//        }
        return mapOf<String, Any?>("sample message" to null)
//        return Void;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun a(exc: java.lang.Exception) {}


}