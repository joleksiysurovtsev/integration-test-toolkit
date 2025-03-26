package dev.surovtsev.integration.test.toolkit.tool.kafka

import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaEventDefinition

/**
 * ExampleKafkaEvent is a sample implementation of KafkaEventDefinition used for integration or unit testing with Kafka.
 *
 * This class provides a basic example of how to implement the KafkaEventDefinition interface
 * for testing Kafka messaging workflows. It uses String as the payload type for simplicity.
 *
 * Usage example:
 *
 * ```kotlin
 * // In a Spring configuration class
 * @Bean
 * fun testEvent(): KafkaEventDefinition<String> = ExampleKafkaEvent()
 *
 * // Using with KafkaEventRegistry
 * val registry = KafkaEventRegistry(listOf(ExampleKafkaEvent()))
 * ```
 */

class ExampleKafkaEvent : KafkaEventDefinition<String> {
    /**
     * The unique identifier for this event type.
     * Set to "test" for testing purposes.
     */
    override val eventType = "test"

    /**
     * The Kafka topic name where this event will be published.
     * Set to "EVENT_TOPIC" for testing purposes.
     */
    override val topicName = "EVENT_TOPIC"

    /**
     * The Kotlin class representing the structure of the event payload.
     * Uses String class for simplicity in testing.
     */
    override val kClass = String::class

    /**
     * The identifier of the system or service that originates this message.
     * Set to "test" for testing purposes.
     */
    override val messageOriginator = "test"
}
