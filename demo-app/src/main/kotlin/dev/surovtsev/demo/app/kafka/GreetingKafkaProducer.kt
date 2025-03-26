package dev.surovtsev.demo.app.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GreetingKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${kafka.topic.greetings:greetings}") private val topic: String
) {

    fun sendGreeting(name: String) {
        val actionId = UUID.randomUUID().toString()
        val greeting = Greeting(message = "Hello, $name!")
        val payload = objectMapper.writeValueAsString(greeting)

        // Create a message with headers
        val message = MessageBuilder.withPayload(payload)
            .setHeader(KafkaHeaders.KEY, actionId)
            .setHeader(KafkaHeaders.TOPIC, topic)
            .setHeader("actionId", actionId)
            .setHeader("actionType", "greeting")
            .setHeader("messageOriginator", "demo-app")
            .build()

        // Send the message
        kafkaTemplate.send(message)
    }

    data class Greeting(val message: String)
}
