package net.piotrturski.shop

import com.fasterxml.jackson.databind.SerializationFeature
import net.piotrturski.shop.order.ExceptionMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.matcher.AssertionMatcher
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import reactor.core.publisher.Flux

/**
 * performs async request. allows adding expectation for async result
 */
fun MockMvc.async(builder: RequestBuilder): ResultActions {
    val perform = this.perform(builder)
    return perform
            .andReturn().run { this@async.perform(MockMvcRequestBuilders.asyncDispatch(this)) }
}

fun MockMvc.exchangeOnly(builder: RequestBuilder): ResultActions {
    val resultFromControllerThread = this.perform(builder)
    return if (resultFromControllerThread.andReturn().request.isAsyncStarted)
        this.perform(MockMvcRequestBuilders.asyncDispatch(resultFromControllerThread.andReturn()))
    else
        resultFromControllerThread
}

fun MockMvc.exchange(builder: RequestBuilder): ResultActions = exchangeOnly(builder)
        .andExpect(forwardedUrl(null))
        .andExpect{ if(it.response.status != HttpStatus.NO_CONTENT.value())
                        content().contentTypeCompatibleWith(APPLICATION_JSON_UTF8)}


fun MockHttpServletRequestBuilder.jsonContent(content: String) =
        this.contentType(MediaType.APPLICATION_JSON_UTF8).content(content)

fun jsonPath(path: String, block: (Any) -> Unit): ResultMatcher {
    return MockMvcResultMatchers.jsonPath(path, object : AssertionMatcher<Any>() {
        override fun assertion(actual: Any) {
            block(actual)
        }
    })
}

fun restMockMvc(vararg controllers: Any): MockMvc {
    return MockMvcBuilders.standaloneSetup(*controllers, ExceptionMapper())
            .alwaysDo<StandaloneMockMvcBuilder>(MockMvcResultHandlers.print())
            //from: AllEncompassingFormHttpMessageConverter.class
            .setMessageConverters(MappingJackson2HttpMessageConverter(
                    Jackson2ObjectMapperBuilder.json()
                            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .build()))
//            .setMessageConverters(TestMessageConverters{
//                    featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)})
            .alwaysExpect<StandaloneMockMvcBuilder>(forwardedUrl(null))
            .build()
}

fun <T> forEach(vararg element: T, body:(T) -> Unit) = element.forEach (body)
fun <A,B>forEachCrossProduct(list1:List<A>, list2:List<B>, action:(A, B) -> Unit) =
        list1.flatMap { a -> list2.map { Pair(a, it) } }.forEach{(a,b) -> action(a,b)}

fun <T> assertThat(flux: Flux<T>) = assertThat(flux.toStream())