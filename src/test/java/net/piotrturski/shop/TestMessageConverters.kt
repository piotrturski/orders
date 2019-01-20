package net.piotrturski.shop

import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter


// doesn't work a expected
class TestMessageConverters(objectMapperConfig: Jackson2ObjectMapperBuilder.()->Jackson2ObjectMapperBuilder)
    : AllEncompassingFormHttpMessageConverter() {

    init {
        super.addPartConverter(MappingJackson2HttpMessageConverter(
                Jackson2ObjectMapperBuilder.json().run(objectMapperConfig).build()))
    }

    override fun addPartConverter(partConverter: HttpMessageConverter<*>) {
        if (partConverter !is MappingJackson2HttpMessageConverter)
            super.addPartConverter(partConverter)
    }
}
