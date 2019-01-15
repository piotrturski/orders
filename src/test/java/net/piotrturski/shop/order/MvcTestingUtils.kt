package net.piotrturski.shop.order

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.matcher.AssertionMatcher
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
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
//            .alwaysExpect<StandaloneMockMvcBuilder>(forwardedUrl(null))
//            .alwaysExpect<StandaloneMockMvcBuilder>(content().contentTypeCompatibleWith(APPLICATION_JSON_UTF8))
            .build()
}

fun <T> forEach(vararg element: T, body:(T) -> Unit) = element.forEach (body)