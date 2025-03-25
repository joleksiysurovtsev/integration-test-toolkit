package dev.surovtsev.integration.test.toolkit.containers.kafka

import kotlin.reflect.KClass


/**
 * Defines a Kafka event with associated metadata used for messaging workflows.
 *
 * Implementations of [KafkaEventDefinition] should provide the following properties:
 * - [eventType]: A unique identifier representing the event type.
 * - [topicName]: The Kafka topic associated with the event.
 * - [kClass]: The Kotlin class representing the structure of the event payload.
 * - [messageOriginator]: An optional property specifying the originating system or service of the message.
 *
 * ### Example implementation:
 * ```kotlin
 * object UserRegisteredEvent : KafkaEventDefinition<UserRegisteredPayload> {
 *     override val eventType = "USER_REGISTERED"
 *     override val topicName = "USER_EVENTS"
 *     override val kClass = UserRegisteredPayload::class
 *     override val messageOriginator = "user-service"
 * }
 * ```
 */
interface KafkaEventDefinition<T : Any> {
    val eventType: String
    val topicName: String
    val kClass: KClass<T>
    val messageOriginator: String?
}

