package dev.surovtsev.integration.test.toolkit.containers.kafka

import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName
import org.testcontainers.containers.KafkaContainer

class IntegrationKafkaContainer private constructor(network: Network?) :
    KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
            .asCompatibleSubstituteFor("confluentinc/cp-kafka")
    ) {

    init {
        this
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:29092,BROKER://:9092")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,BROKER:PLAINTEXT")
            .start()
    }

    companion object {
        @Volatile
        private var INSTANCE: IntegrationKafkaContainer? = null
        private var BOOTSTRAP: String? = null

        @Synchronized
        fun getInstance(network: Network? = dev.surovtsev.integration.test.toolkit.containers.Network.getInstance()): IntegrationKafkaContainer =
            INSTANCE ?: IntegrationKafkaContainer(network).also { INSTANCE = it }

        fun getBootstrap(): String =
            BOOTSTRAP ?: getInstance().bootstrapServers.also { BOOTSTRAP = it }
    }
}