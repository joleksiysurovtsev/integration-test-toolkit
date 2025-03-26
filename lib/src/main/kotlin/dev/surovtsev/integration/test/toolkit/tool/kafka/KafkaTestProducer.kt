package dev.surovtsev.integration.test.toolkit.tool.kafka

import dev.surovtsev.integration.test.toolkit.containers.kafka.IntegrationKafkaContainer
import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaEventDefinition
import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaHeaders
import org.apache.kafka.clients.producer.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.*


object KafkaTestProducer {

	private val logger = LoggerFactory.getLogger(KafkaTestProducer::class.java)
	private val producer = createProducer()

    private fun createProducer(): KafkaProducer<String, String> {
        return KafkaProducer(getProperties())
    }

    private fun getProperties(): Properties {
        return Properties().apply {
            this[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = IntegrationKafkaContainer.getBootstrap()
            this[ProducerConfig.CLIENT_ID_CONFIG] = "intTest-${UUID.randomUUID()}"
            this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
            this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        }
    }

    private class TestCallback : Callback {
		override fun onCompletion(recordMetadata: RecordMetadata, e: Exception?) {
			if (e != null) {
				logger.error("Error during TestCallback to topic: $recordMetadata")
				e.printStackTrace()
			}
		}
	}

    fun <T: Any> sendMessages(event: KafkaEventDefinition<T>, body: String, headers: KafkaHeaders?) {
        val callback = TestCallback()
        val recordHeaders = RecordHeaders()
        headers?.toMap()?.forEach { (key, value) -> recordHeaders.add(key, value.toByteArray()) }
        recordHeaders.add(event.eventType, event.eventType.toByteArray())
        recordHeaders.add("contentType", "application/json".toByteArray())
        val data = ProducerRecord(event.topicName, null, headers?.actionId?.toString() ?: UUID.randomUUID().toString(), body, recordHeaders)
        producer.send(data, callback)
        logger.info("Successfully sent message ${event.eventType} to topic ${event.topicName}")
    }
}
