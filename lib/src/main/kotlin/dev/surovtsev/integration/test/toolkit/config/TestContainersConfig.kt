package dev.surovtsev.integration.test.toolkit.config

import dev.surovtsev.integration.test.toolkit.containers.PostgresContainer
import dev.surovtsev.integration.test.toolkit.containers.kafka.EventDataSource
import dev.surovtsev.integration.test.toolkit.containers.kafka.IntegrationKafkaContainer
import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaEventDefinition
import dev.surovtsev.integration.test.toolkit.containers.kafka.KafkaEventRegistry
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
open class TestContainersConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = ["test.postgres.enabled"], havingValue = "true", matchIfMissing = false)
    open fun postgresContainer(): PostgresContainer = PostgresContainer.getInstance()

    @Bean
    @ConditionalOnBean(PostgresContainer::class)
    open fun dataSource(postgresContainer: PostgresContainer): DataSource =
        DataSourceBuilder.create()
            .url(postgresContainer.jdbcUrl)
            .username(postgresContainer.username)
            .password(postgresContainer.password)
            .build()

    @Bean
    @ConditionalOnBean(DataSource::class)
    open fun jdbcTemplate(dataSource: DataSource): JdbcTemplate = JdbcTemplate(dataSource)

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = ["test.kafka.enabled"], havingValue = "true", matchIfMissing = false)
    open fun kafkaContainer(): IntegrationKafkaContainer = IntegrationKafkaContainer.getInstance()

    @Bean
    @ConditionalOnProperty(name = ["test.kafka.enabled"], havingValue = "true", matchIfMissing = false)
    @DependsOn("kafkaContainer")
    open fun kafkaEventRegistry(events: List<KafkaEventDefinition<*>>): KafkaEventRegistry =
        KafkaEventRegistry(events)

    @Bean
    @ConditionalOnProperty(
        name = ["test.kafka.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @DependsOn("kafkaEventRegistry")
    open fun eventDataSource(kafkaEventRegistry: KafkaEventRegistry): EventDataSource =
        EventDataSource(kafkaEventRegistry)
}