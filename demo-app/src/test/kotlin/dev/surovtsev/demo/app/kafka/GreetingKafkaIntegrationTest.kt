package dev.surovtsev.demo.app.kafka

import dev.surovtsev.demo.app.config.TestConfig
import dev.surovtsev.integration.test.toolkit.IntegrationTestBase
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration

@Import(TestConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GreetingKafkaIntegrationTest : IntegrationTestBase() {

    @BeforeAll
    fun enableKafka() {
        // Enable Kafka for this test
        System.setProperty("test.kafka.enabled", "true")
    }

    @Test
    fun `should send greeting event to Kafka when calling greeting endpoint`() {
        // Given
        val name = "TestUser"

        // When - Call the greeting endpoint
        mockMvc.perform(get("/greeting").param("name", name))
            .andExpect(status().isOk)

        // Then - Verify that the greeting event was sent to Kafka
        await.atMost(Duration.ofSeconds(10)).untilAsserted {
            val events = kafkaEventDataSource.findEvents(GreetingKafkaEventDefinition)
            assert(events.isNotEmpty()) { "No greeting events found in Kafka" }

            val greeting = events.first().first
            assert(greeting.message == "Hello, $name!") { 
                "Expected greeting message 'Hello, $name!', but got '${greeting.message}'" 
            }
        }
    }
}
