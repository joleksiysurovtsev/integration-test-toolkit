package dev.surovtsev.demo.app.config

import dev.surovtsev.demo.app.kafka.GreetingKafkaEventDefinition
import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaEventDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TestConfig {

    @Bean
    open fun greetingKafkaEventDefinition(): KafkaEventDefinition<*> {
        return GreetingKafkaEventDefinition
    }
}
