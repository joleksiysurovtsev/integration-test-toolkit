package dev.surovtsev.integration.test.toolkit.tool.kafka

import com.zaxxer.hikari.HikariDataSource
import dev.surovtsev.integration.test.toolkit.containers.kafka.IntegrationKafkaContainer
import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaEventDefinition
import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaEventRegistry
import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaHeaders
import dev.surovtsev.integration.test.toolkit.json.JsonUtil
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import javax.sql.DataSource

@Component
class KafkaEventDataSource(
	private val kafkaEventRegistry: KafkaEventRegistry
) {

	private val jdbcTemplate: JdbcTemplate


	init {
		val dataSource = createInMemoryDataSource()
		jdbcTemplate = JdbcTemplate(dataSource)

		createEventsTable()
		setupConsumerLoop()
	}

	/**
	 * Create in-memory H2 DataSource with PostgreSQL-compatible mode
	 */
	private fun createInMemoryDataSource(): DataSource {
		return DataSourceBuilder.create()
			.type(HikariDataSource::class.java)
			.driverClassName("org.h2.Driver")
			.url("jdbc:h2:mem:integration-events;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
			.username("sa")
			.password("")
			.build()
	}

	/**
	 * Creates a table of events if it does not already exist
	 */
	private fun createEventsTable() {
		jdbcTemplate.execute(
			"""
            CREATE TABLE IF NOT EXISTS events (
                id SERIAL PRIMARY KEY NOT NULL,
                topic VARCHAR(100),
                action_id VARCHAR(50),
                parent_action_id VARCHAR(50),
                action_type VARCHAR(255),
                message_originator VARCHAR(255),
                data TEXT NOT NULL,
                headers TEXT NOT NULL
            );
            CREATE INDEX IF NOT EXISTS ACTION_IDX ON events (topic, action_type);
            """.trimIndent()
		)
	}

	/**
	 * Saves the event from Kafka to the events table if actionType is supported
	 */
	private fun saveEvent(record: ConsumerRecord<String, String>) {
		try {
			val headers = record.headers().associate { it.key() to String(it.value()) }
			if (headers["actionType"] in kafkaEventRegistry.supportedEvents) {
				val actionId = record.key()
				jdbcTemplate.update(
					"""
                    INSERT INTO events (
                        topic, action_id, parent_action_id, action_type, message_originator, data, headers
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
					record.topic(),
					actionId,
					headers["parentActionId"],
					headers["actionType"],
					headers["messageOriginator"],
					record.value(),
					JsonUtil.getAsString(headers)
				)
			}
		} catch (t: Throwable) {
			t.printStackTrace()
		}
	}

	/**
	 * Runs a separate Kafka user stream, saving events to the table
	 */
	private fun setupConsumerLoop() {
		val consumer = KafkaConsumer<String, String>(getProperties())
		consumer.subscribe(kafkaEventRegistry.topics)
		Thread {
			while (true) {
				try {
					consumer.poll(Duration.ofSeconds(2)).forEach { event ->
						saveEvent(event)
					}
				} catch (t: Throwable) {
					t.printStackTrace()
				}

			}
		}.start()
	}

	/**
	 * Returns the Kafka consumer-a properties
	 */
	private fun getProperties(): Properties {
		return Properties().apply {
			this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = IntegrationKafkaContainer.getBootstrap()
			this[ConsumerConfig.GROUP_ID_CONFIG] = "consumerLoop-itTest"
			this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
			this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
			this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
			this[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "100"
			this[ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG] = ConsumerConfig.DEFAULT_FETCH_MAX_BYTES.times(5)
		}
	}

	/**
	 * Returns all events as specified
	 */
	fun <T : Any> findEvents(eventDef: KafkaEventDefinition<T>): List<Pair<T, KafkaHeaders>> {
		val result = jdbcTemplate.queryForList(
			"SELECT data, headers FROM events WHERE topic=? AND action_type=? AND message_originator=?",
			eventDef.topicName, eventDef.eventType, eventDef.messageOriginator
		)

		return result.map {
			JsonUtil.getFromJson(it["DATA"].toString(), eventDef.kClass, true) to
					JsonUtil.getFromJson(it["HEADERS"].toString(), KafkaHeaders::class, true)
		}
	}

	/**
	 * Returns all events by parentActionId
	 */
	fun <T : Any> findEvents(eventDef: KafkaEventDefinition<T>, parentActionId: String): List<Pair<T, KafkaHeaders>> {
		val result = jdbcTemplate.queryForList(
			"SELECT data, headers FROM events WHERE topic=? AND action_type=? AND action_id=? AND message_originator=?",
			eventDef.topicName, eventDef.eventType, parentActionId, eventDef.messageOriginator
		)

		return result.map {
			JsonUtil.getFromJson(it["DATA"].toString(), eventDef.kClass, true) to
					JsonUtil.getFromJson(it["HEADERS"].toString(), KafkaHeaders::class, true)
		}
	}
}