package dev.surovtsev.integration.test.toolkit

import dev.surovtsev.integration.test.toolkit.config.TestContainersConfig
import dev.surovtsev.integration.test.toolkit.containers.PostgresContainer
import dev.surovtsev.integration.test.toolkit.containers.kafka.IntegrationKafkaContainer
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

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestContainersConfig::class)
abstract class IntegrationTestBase {

    companion object {

        private val isKafkaEnabled: Boolean = System.getProperty("test.kafka.enabled", "false").toBoolean()
        private val isPostgresEnabled: Boolean = System.getProperty("test.postgres.enabled", "false").toBoolean()

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

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired(required = false)
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired(required = false)
    lateinit var dataSource: DataSource

    @BeforeEach
    fun setup() {
        if (this::jdbcTemplate.isInitialized) {
            executeSqlScriptsFromDirectory("/sql/before/")
        }
    }

    @AfterEach
    fun cleanup() {
        if (this::jdbcTemplate.isInitialized) {
            executeSqlScriptsFromDirectory("/sql/after/")
        }
    }

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