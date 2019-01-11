package net.piotrturski.infra

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions
import org.junit.Test

class JsonPathTest {

    @Test
    fun `should filter objects in top level array by field value`() {
        val read = JsonPath.parse("""[{"field":"price", "error":"a text"},{"field":"price", "error":"text2"}]""")
                .read<Any>("[?(@.field=='price')].error")

        Assertions.assertThat(read as List<String>).containsExactly("a text", "text2")
    }

}