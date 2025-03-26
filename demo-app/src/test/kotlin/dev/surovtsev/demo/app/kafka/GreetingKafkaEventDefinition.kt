package dev.surovtsev.demo.app.kafka

import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaEventDefinition
import kotlin.reflect.KClass

/**
 * Definition of the Greeting Kafka event for testing purposes.
 * This is used by the integration test toolkit to identify and deserialize Kafka events.
 */
object GreetingKafkaEventDefinition : KafkaEventDefinition<GreetingKafkaProducer.Greeting> {
    override val eventType: String = "greeting"
    override val topicName: String = "greetings"
    override val messageOriginator: String = "demo-app"
    override val kClass: KClass<GreetingKafkaProducer.Greeting> = GreetingKafkaProducer.Greeting::class
}
