package dev.surovtsev.integration.test.toolkit.tool.kafka

import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaEventDefinition
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import java.util.*
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory


object KafkaEventReceiver {

    private val logger = LoggerFactory.getLogger(KafkaEventReceiver::class.java)

    lateinit var kafkaEventDataSource: KafkaEventDataSource

    fun <T : Any> awaitAnyEvent(event: KafkaEventDefinition<T>): T {
        lateinit var result: T

        awaitBlock("Should have ${event.eventType}!") {
            val events = kafkaEventDataSource.findEvents(event)
            assertThat(events).isNotEmpty
            result = events.first().first
        }

        return result
    }

    fun <T : Any> awaitEvent(actionId: UUID, event: KafkaEventDefinition<T>) {
        awaitBlock("Should have ${event.eventType} for actionId=$actionId") {
            val events = kafkaEventDataSource.findEvents<T>(event, actionId.toString())
            assertThat(events).isNotEmpty
        }
    }

    fun <T : Any> awaitResultEvent(actionId: UUID, event: KafkaEventDefinition<T>): T {
        val start = System.currentTimeMillis()

        val result = await("Should have ${event.eventType} for actionId=$actionId")
            .pollInterval(10, TimeUnit.SECONDS)
            .atMost(120, TimeUnit.SECONDS)
            .untilCallTo {
                kafkaEventDataSource.findEvents<T>(event, actionId.toString())
            }.matches {
                it?.isEmpty() == false
            }!!.first().first

        val duration = System.currentTimeMillis() - start
        logger.info("✅ Event ${event.eventType} received for actionId=$actionId in ${duration}ms")

        return result
    }

    fun awaitFailureEvent(actionId: UUID, event: KafkaEventDefinition<EventError>, errorMessage: String) {
        awaitBlock("Should have ${event.eventType} failure for actionId=$actionId") {
            val entity = kafkaEventDataSource.findEvents(event, actionId.toString())
            assertThat(entity).isNotEmpty
            assertThat(entity.first().first.error).isEqualTo(errorMessage)
        }
    }

    private fun awaitBlock(description: String, block: () -> Unit) {
        val start = System.currentTimeMillis()
        logger.info("⏳ Awaiting: $description")

        await(description)
            .pollInterval(10, TimeUnit.SECONDS)
            .atMost(120, TimeUnit.SECONDS)
            .untilAsserted { block() }

        val duration = System.currentTimeMillis() - start
        logger.info("✅ $description completed in ${duration}ms")
    }
}
