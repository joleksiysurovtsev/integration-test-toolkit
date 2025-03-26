package dev.surovtsev.integration.test.toolkit.tool.postgres

import dev.surovtsev.integration.test.toolkit.containers.PostgresContainer
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor


/**
 * PgQueryTool is a lightweight SQL utility used to simplify direct database access in tests.
 *
 * It enables mapping of PostgreSQL rows to Kotlin data classes using reflection.
 *
 * The data classes should follow this convention:
 *  - Optionally annotate fields with `@Column(name = "...")` for clarity.
 *  - Define SQL queries inside the `companion object` (see [ExamplePgEntity]).
 *
 * Example:
 * ```kotlin
 * val entity = PgQueryTool.queryForEntity<ExamplePgEntity>(
 *     ExamplePgEntity.getByIdQuery(),
 *     listOf(UUID.fromString("..."))
 * )
 *
 * val allEntities = PgQueryTool.queryForListEntities<ExamplePgEntity>(
 *     ExamplePgEntity.getAllQuery()
 * )
 * ```
 */
object PgQueryTool {

    /**
     * Returns a single entity from the database, or null if no result found.
     */
    inline fun <reified T : Any> queryForEntity(
        query: String,
        parameters: List<Any> = emptyList()
    ): T? {
        val container = PostgresContainer.getInstance()
        DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { conn ->
            conn.prepareStatement(query).use { stmt ->
                parameters.forEachIndexed { i, param -> stmt.setObject(i + 1, param) }
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) {
                        mapResultSetToEntity(rs, T::class)
                    } else null
                }
            }
        }
    }

    /**
     * Returns a list of entities from the database.
     */
    inline fun <reified T : Any> queryForListEntities(
        query: String,
        parameters: List<Any> = emptyList()
    ): List<T> {
        val result = mutableListOf<T>()
        val container = PostgresContainer.getInstance()
        DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { conn ->
            conn.prepareStatement(query).use { stmt ->
                parameters.forEachIndexed { i, param -> stmt.setObject(i + 1, param) }
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        result.add(mapResultSetToEntity(rs, T::class))
                    }
                }
            }
        }
        return result
    }

    /**
     * Uses reflection to create an entity from a ResultSet row.
     * Assumes camelCase property names map to snake_case column names.
     */
    fun <T : Any> mapResultSetToEntity(rs: ResultSet, clazz: KClass<T>): T {
        val constructor = clazz.primaryConstructor ?: error("No primary constructor for ${clazz.simpleName}")
        val args = mutableMapOf<KParameter, Any?>()
        for (param in constructor.parameters) {
            val column = camelToSnake(param.name!!)
            args[param] = rs.getObject(column)
        }
        return constructor.callBy(args)
    }

    /**
     * Converts camelCase to snake_case (e.g., userId → user_id).
     */
    private fun camelToSnake(str: String): String =
        str.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
}