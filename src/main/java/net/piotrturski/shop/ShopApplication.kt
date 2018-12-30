package net.piotrturski.shop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.schema.AlternateTypeRule
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.time.Clock

@SpringBootApplication
@EnableSwagger2
class ShopApplication {

    @Bean fun clock() = Clock.systemUTC()

    @Bean fun swagger() = Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage(ShopApplication::class.java.`package`.name))
            .paths(PathSelectors.any())
            .build()
}

fun main(args: Array<String>) {
    runApplication<ShopApplication>(*args)
}

