package dev.surovtsev.demo.app.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import dev.surovtsev.demo.app.kafka.domain.EventError
import dev.surovtsev.demo.app.kafka.domain.EventMessage
import dev.surovtsev.demo.app.kafka.domain.SampleTopicRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaListenerService(
    private val routes: List<SampleTopicRoute<*, *>>,
    private val objectMapper: ObjectMapper,
    private val kafkaResponseService: KafkaResponseService,
    eventListenerDispatcher: ExecutorCoroutineDispatcher,
) {

    private val log = LoggerFactory.getLogger(this::class.java)
    private val scope = CoroutineScope(eventListenerDispatcher)

    companion object {
        private const val TOPIC = "EVENT_TOPIC"
    }

    @KafkaListener(topics = [TOPIC], groupId = "billing-service")
    fun onMessage(record: ConsumerRecord<String, ByteArray>) {
        val event = buildEvent(record) ?: return

        routes.filter { it.supports(event.actionType) }
            .forEach { route ->
                scope.launch {
                    handleRoute(route, event)
                }
            }
    }

    private fun buildEvent(record: ConsumerRecord<String, ByteArray>): EventMessage<ByteArray>? {
        return runCatching {
            val headers = record.headers()
            val customHeaders = getCustomHeaders(headers)

            EventMessage(
                payload = record.value(),
                actionId = customHeaders.resolveKey("actionId"),
                actionType = customHeaders.resolveKey("actionType"),
                parentActionId = customHeaders["parentActionId"],
                messageOriginator = customHeaders["messageOriginator"],
            )
        }.onFailure {
            log.error("Failed to parse EventMessage from record: ${it.message}", it)
        }.getOrNull()
    }

    private fun <T, S> handleRoute(route: SampleTopicRoute<T, S>, event: EventMessage<ByteArray>) {
        val messageEvent = convertFromByteArray(event, route.payloadClass) ?: return

        runCatching {
            route.apply(messageEvent)
        }.onSuccess { result ->
            log.debug("Successfully processed route: ${route::class.simpleName}")
            kafkaResponseService.sendResponse(messageEvent, result, KafkaResponseService.SUCCESS_POSTFIX)
        }.onFailure { error ->
            log.error("Error in route ${route::class.simpleName}: ${error.message}", error)
            kafkaResponseService.sendResponse(
                messageEvent, 
                EventError(error.message, error.localizedMessage), 
                KafkaResponseService.FAILURE_POSTFIX
            )
        }
    }

    private fun <T> convertFromByteArray(event: EventMessage<ByteArray>, clazz: Class<T>): EventMessage<T>? {
        return runCatching {
            EventMessage(
                payload = objectMapper.readValue(event.payload, clazz),
                actionType = event.actionType,
                actionId = event.actionId,
                parentActionId = event.parentActionId,
                messageOriginator = event.messageOriginator,
            )
        }.onFailure {
            log.error("Failed to deserialize payload to ${clazz.simpleName}: ${it.message}", it)
        }.getOrNull()
    }

    private fun getCustomHeaders(headers: Headers): Map<String, String> {
        val map = mutableMapOf<String, String>()
        headers.forEach {
            val value = it.value()
            if (value != null) {
                map[it.key()] = String(value, Charsets.UTF_8)
            }
        }
        return map
    }

    private fun Map<String, Any>.resolveKey(key: String): String {
        return this[key]?.toString()
            ?: throw IllegalArgumentException("Missing required Kafka header: $key")
    }
}
