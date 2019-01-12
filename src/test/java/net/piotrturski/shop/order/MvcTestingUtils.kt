package net.piotrturski.shop.order

import org.assertj.core.matcher.AssertionMatcher
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
fun MockMvc.exchange(builder: RequestBuilder): ResultActions {
    val perform = this.perform(builder)
    return perform
            .andReturn().run { this@exchange.perform(MockMvcRequestBuilders.asyncDispatch(this)) }
}

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
            .alwaysExpect<StandaloneMockMvcBuilder>(forwardedUrl(null))
            .alwaysExpect<StandaloneMockMvcBuilder>(content().contentTypeCompatibleWith(APPLICATION_JSON_UTF8))
//                .setHandlerExceptionResolvers(DefaultHandlerExceptionResolver())
//                .setMessageConverters(MappingJackson2HttpMessageConverter())
            .build()
}