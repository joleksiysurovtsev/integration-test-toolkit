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
