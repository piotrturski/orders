package net.piotrturski.shop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.Clock

@SpringBootApplication
class ShopApplication {
    @Bean fun clock() = Clock.systemUTC()

}

fun main(args: Array<String>) {
    runApplication<ShopApplication>(*args)
}

