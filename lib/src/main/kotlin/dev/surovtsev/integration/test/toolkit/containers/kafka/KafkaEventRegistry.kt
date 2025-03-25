package dev.surovtsev.integration.test.toolkit.containers.kafka

import org.springframework.stereotype.Component

@Component
class KafkaEventRegistry(
    events: List<KafkaEventDefinition<*>>
) {
    val eventDefinitions: Map<String, KafkaEventDefinition<*>> = events.associateBy { it.eventType }

    fun getByEventType(eventType: String): KafkaEventDefinition<*>? = eventDefinitions[eventType]

    val topics: Set<String> = events.map { it.topicName }.toSet()
    val supportedEvents: Set<String> = eventDefinitions.keys
}


@Component
class TestEvent : KafkaEventDefinition<String> {
    override val eventType = "test"
    override val topicName = "test"
    override val kClass = String::class
    override val messageOriginator = "test"
}