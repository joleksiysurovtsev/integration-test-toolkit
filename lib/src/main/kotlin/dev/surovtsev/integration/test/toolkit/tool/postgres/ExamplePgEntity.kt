package dev.surovtsev.integration.test.toolkit.tool.postgres

import jakarta.persistence.Column
import java.sql.Timestamp
import java.util.*

/**
 * ExamplePgEntity is a sample technical entity used for integration or unit testing against a PostgreSQL database.
 *
 * This class follows a recommended practice for use with PgQueryTool: define queries inside the `companion object`.
 * This approach makes your code reusable, self-contained, and cleaner when writing tests or scripts.
 *
 * Usage example:
 *
 * ```kotlin
 * val entity = PgQueryTool.queryForEntity<ExamplePgEntity>(
 *     ExamplePgEntity.getByIdQuery(),
 *     listOf(UUID.fromString("..."))
 * )
 * ```
 */
data class ExamplePgEntity(
    @Column(name = "id")
    val id: UUID,

    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "email")
    val email: String,

    @Column(name = "status")
    val status: String,

    @Column(name = "created_at")
    val createdAt: Timestamp,

    @Column(name = "updated_at")
    val updatedAt: Timestamp? = null
) {
    companion object {

        /**
         * SQL query to select a single account by its ID.
         */
        fun getByIdQuery(): String = """
            SELECT 
                id,
                user_id,
                email,
                status,
                created_at,
                updated_at
            FROM example_pg_entity
            WHERE id = ?
        """.trimIndent()

        /**
         * SQL query to select all accounts.
         */
        fun getAllQueryQuery(): String = """
            SELECT 
                id,
                user_id,
                email,
                status,
                created_at,
                updated_at
            FROM example_pg_entity
        """.trimIndent()
    }
}