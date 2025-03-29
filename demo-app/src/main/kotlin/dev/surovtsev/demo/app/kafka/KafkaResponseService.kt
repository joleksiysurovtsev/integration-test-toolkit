package dev.surovtsev.demo.app.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import dev.surovtsev.demo.app.kafka.domain.EventMessage
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaResponseService(
    private val objectMapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Value("\${spring.application.name}")
    private lateinit var applicationName: String

    companion object {
        private const val TOPIC = "EVENT_TOPIC"
        const val SUCCESS_POSTFIX = "_SUCCESS"
        const val FAILURE_POSTFIX = "_FAILURE"
    }

    fun <S> sendResponse(event: EventMessage<*>, response: S, postfix: String) {
        val responseMessage = EventMessage(
            actionType = event.actionType + postfix,
            actionId = UUID.randomUUID().toString(),
            parentActionId = event.parentActionId ?: event.actionId,
            messageOriginator = applicationName,
            payload = response
        )

        val headers = mapOf(
            "actionType" to responseMessage.actionType,
            "actionId" to responseMessage.actionId,
            "parentActionId" to (responseMessage.parentActionId ?: ""),
            "messageOriginator" to applicationName
        )

        val record = org.apache.kafka.clients.producer.ProducerRecord<String, Any>(
            TOPIC,
            null,
            null,
            null,
            objectMapper.writeValueAsString(responseMessage),
            buildKafkaHeaders(headers)
        )

        kafkaTemplate.send(record)
        log.debug("Sent response for action type: ${event.actionType} with postfix: $postfix")
    }

    private fun buildKafkaHeaders(map: Map<String, String>): Headers {
        val headers = RecordHeaders()
        map.forEach { (key, value) ->
            headers.add(key, value.toByteArray(Charsets.UTF_8))
        }
        return headers
    }
}
