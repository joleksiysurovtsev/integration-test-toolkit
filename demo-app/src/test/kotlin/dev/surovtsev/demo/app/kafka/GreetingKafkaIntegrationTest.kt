package dev.surovtsev.demo.app.kafka

import dev.surovtsev.integration.test.toolkit.IntegrationTestBase
import org.junit.jupiter.api.Test


class GreetingKafkaIntegrationTest : IntegrationTestBase() {

    @Test
    fun failProvidersUnlinkForAccountInsideHierarchy() {
        // Enable Kafka for this test
        System.setProperty("test.kafka.enabled", "true")
    }

}
