package dev.surovtsev.integration.test.toolkit

import dev.surovtsev.integration.test.toolkit.config.TestContainersConfig
import dev.surovtsev.integration.test.toolkit.containers.PostgresContainer
import dev.surovtsev.integration.test.toolkit.containers.kafka.IntegrationKafkaContainer
import dev.surovtsev.integration.test.toolkit.tool.kafka.KafkaEventDataSource
import dev.surovtsev.integration.test.toolkit.tool.kafka.KafkaEventReceiver
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import java.net.JarURLConnection
import java.nio.file.Files
import java.nio.file.Paths
import javax.sql.DataSource

/**
 * Base class for integration tests that provides infrastructure for testing with PostgreSQL and Kafka.
 *
 * This abstract class sets up a Spring Boot test environment with MockMvc and configures test containers
 * for PostgreSQL and Kafka when enabled. It provides utilities for executing SQL scripts before and after
 * each test, and configures dynamic properties for Spring to connect to the test containers.
 *
 * ## Features:
 * - Configures Spring Boot test environment with MockMvc
 * - Supports PostgreSQL container (enabled with system property `test.postgres.enabled=true`)
 * - Supports Kafka container (enabled with system property `test.kafka.enabled=true`)
 * - Automatically executes SQL scripts from classpath resources before and after each test
 * - Works with both filesystem and JAR resources for SQL scripts
 *
 * ## Usage:
 * 1. Create a test class that extends IntegrationTestBase
 * 2. Place SQL scripts in the classpath resources:
 *    - `/sql/before/` directory for setup scripts (executed before each test)
 *    - `/sql/after/` directory for cleanup scripts (executed after each test)
 * 3. Enable PostgreSQL and/or Kafka containers by setting system properties:
 *    - `-Dtest.postgres.enabled=true`
 *    - `-Dtest.kafka.enabled=true`
 *
 * ## Example:
 * ```kotlin
 * class MyIntegrationTest : IntegrationTestBase() {
 *     @Test
 *     fun testSomething() {
 *         // Use mockMvc, jdbcTemplate, etc.
 *     }
 * }
 * ```
 */
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestContainersConfig::class)
abstract class IntegrationTestBase {

    /**
     * Companion object that handles container configuration and dynamic property registration.
     */
    companion object {
        /**
         * Flag indicating whether Kafka container is enabled.
         * Controlled by system property `test.kafka.enabled` (defaults to false).
         */
        private val isKafkaEnabled: Boolean = System.getProperty("test.kafka.enabled", "false").toBoolean()

        /**
         * Flag indicating whether PostgreSQL container is enabled.
         * Controlled by system property `test.postgres.enabled` (defaults to false).
         */
        private val isPostgresEnabled: Boolean = System.getProperty("test.postgres.enabled", "false").toBoolean()

        /**
         * Registers dynamic properties for Spring to connect to test containers.
         * 
         * This method is called by Spring's DynamicPropertyRegistry mechanism to set up
         * connection properties for PostgreSQL and Kafka containers when they are enabled.
         *
         * @param registry The Spring DynamicPropertyRegistry to register properties with
         */
        @JvmStatic
        @DynamicPropertySource
        fun dynamicProperties(registry: DynamicPropertyRegistry) {
            if (isPostgresEnabled) {
                registry.add("spring.datasource.url") { PostgresContainer.getJdbcUrl() }
                registry.add("spring.datasource.username") { PostgresContainer.getJdbcUsername() }
                registry.add("spring.datasource.password") { PostgresContainer.getJdbcPassword() }
            } else {
                println("Postgres Container is disabled (test.postgres.enabled=false).")
            }

            if (isKafkaEnabled) {
                registry.add("spring.kafka.bootstrap-servers") { IntegrationKafkaContainer.getBootstrap() }
            } else {
                println("Kafka Container is disabled (test.kafka.enabled=false).")
            }
        }
    }

    /**
     * MockMvc instance for testing REST endpoints.
     * Automatically configured by Spring's AutoConfigureMockMvc.
     */
    @Autowired
    lateinit var mockMvc: MockMvc

    /**
     * JdbcTemplate for database operations.
     * Only available when PostgreSQL container is enabled.
     */
    @Autowired(required = false)
    lateinit var jdbcTemplate: JdbcTemplate

    /**
     * DataSource for direct database access.
     * Only available when PostgreSQL container is enabled.
     */
    @Autowired(required = false)
    lateinit var dataSource: DataSource


    @Autowired(required = false)
    lateinit var kafkaEventDataSource: KafkaEventDataSource

    /**
     * Sets up the test environment before each test.
     * 
     * If PostgreSQL is enabled, executes all SQL scripts from the `/sql/before/` directory
     * in alphabetical order.
     */
    @BeforeEach
    fun setup() {
        if (this::jdbcTemplate.isInitialized) {
            executeSqlScriptsFromDirectory("/sql/before/")
        }

        if (this::kafkaEventDataSource.isInitialized) {
            KafkaEventReceiver.kafkaEventDataSource = kafkaEventDataSource
        }
    }

    /**
     * Cleans up the test environment after each test.
     * 
     * If PostgreSQL is enabled, executes all SQL scripts from the `/sql/after/` directory
     * in alphabetical order.
     */
    @AfterEach
    fun cleanup() {
        if (this::jdbcTemplate.isInitialized) {
            executeSqlScriptsFromDirectory("/sql/after/")
        }
    }

    /**
     * Gets a sorted list of SQL files from the specified directory path.
     *
     * This method handles both filesystem and JAR resources, finding all .sql files
     * in the specified directory and returning their paths in sorted order.
     *
     * @param directoryPath The path to the directory containing SQL files
     * @return A sorted list of paths to SQL files
     */
    private fun getSortedSqlFiles(directoryPath: String): List<String> {
        val loader = Thread.currentThread().contextClassLoader
        val resourceUrl = loader.getResource(directoryPath.removePrefix("/")) ?: return emptyList()

        return if (resourceUrl.protocol == "jar") {
            val jarConnection = resourceUrl.openConnection() as JarURLConnection
            jarConnection.jarFile.use { jar ->
                jar.entries().toList()
                    .filter { !it.isDirectory && it.name.startsWith(directoryPath.removePrefix("/")) && it.name.endsWith(".sql") }
                    .map { "/${it.name}" }
                    .sorted()
            }
        } else {
            Files.list(Paths.get(resourceUrl.toURI())).use { stream ->
                stream
                    .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".sql") }
                    .sorted()
                    .map { "$directoryPath${it.fileName}" }
                    .toList()
            }
        }
    }

    /**
     * Executes all SQL scripts found in the specified directory.
     *
     * This method finds all .sql files in the given directory, sorts them alphabetically,
     * and executes them using the jdbcTemplate. If no scripts are found, a message is printed.
     *
     * @param directoryPath The path to the directory containing SQL scripts to execute
     * @throws IllegalArgumentException If a script is referenced but not found in classpath resources
     */
    private fun executeSqlScriptsFromDirectory(directoryPath: String) {
        val loader = Thread.currentThread().contextClassLoader
        val scripts = getSortedSqlFiles(directoryPath)
        if (scripts.isEmpty()) {
            println("No SQL scripts found in directory: $directoryPath")
            return
        }

        scripts.forEach { path ->
            val scriptContent = loader.getResourceAsStream(path.removePrefix("/"))
                ?.bufferedReader()?.use { it.readText() }
                ?: throw IllegalArgumentException("Script $path not found in classpath resources")
            jdbcTemplate.execute(scriptContent)
        }
    }
}
