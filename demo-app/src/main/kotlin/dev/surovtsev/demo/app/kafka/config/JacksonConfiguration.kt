package dev.surovtsev.demo.app.kafka.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Configuration
open class JacksonConfiguration {
    @Bean
    @Primary
    open fun objectMapper(): ObjectMapper {
        val javaTimeModule = JavaTimeModule()
            .addDeserializer(
                LocalDateTime::class.java,
                LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )

        return jacksonObjectMapper()
            .registerKotlinModule()
            .registerModule(javaTimeModule)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
}
