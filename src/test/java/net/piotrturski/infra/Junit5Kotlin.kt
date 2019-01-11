package net.piotrturski.infra

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class Junit5Kotlin {

    @Test
    fun test5() {
        assertThat(false).isFalse()
    }
}

class Junit5KotlinOther {

    @Test
    fun test5() {
        assertThat(false).isFalse()
    }

}