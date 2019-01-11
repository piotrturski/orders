package net.piotrturski.infra

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.boot.test.context.SpringBootTest


//@SpringBootTest
class SpekTest: Spek({

    describe("a group") {

        beforeGroup {
            println("before")
        }

        beforeEachTest {
        }

        afterGroup {
            println("after")
        }

        it("1st test in a group") {
            println("test")
            assertThat(false).isFalse()
        }
    }

    describe("a group") {

        it ("2nd test in the same group") {
            println("another test in same group")
            assertThat(false).describedAs("was never run in spek 1").isTrue()
        }
    }

    it("last test") {
//        fail("spek runner test")
    }

    println("end of spec")

})

class Another: Spek({


    describe("a group") {

        it ("3rd test in the same group but different class") {
            println("another test in same group but different class")
            assertThat(true).describedAs("another description").isTrue()
        }
    }

})