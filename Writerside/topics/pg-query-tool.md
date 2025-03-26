# Using PgQueryTool

The `PgQueryTool` is a lightweight SQL utility designed to simplify direct database access in integration tests. It enables easy mapping of PostgreSQL query results to Kotlin data classes using reflection.

## Overview

The `PgQueryTool` provides:
- Simple methods for executing SQL queries against a PostgreSQL database
- Automatic mapping of query results to Kotlin data classes
- Support for parameterized queries to prevent SQL injection

## How It Works

The `PgQueryTool` automatically connects to the PostgreSQL instance provided by `PostgresContainer`, which is a TestContainers instance configured for your integration tests.

## Key Methods

### Querying for a Single Entity

To retrieve a single entity from the database:

```kotlin
val entity = PgQueryTool.queryForEntity<YourEntity>(
    query = "SELECT * FROM your_table WHERE id = ?",
    parameters = listOf(entityId)
)
```

This method will:
- Execute the SQL query with the provided parameters
- Map the first result row to your data class
- Return null if no results are found

### Querying for Multiple Entities

To retrieve a list of entities from the database:

```kotlin
val entities = PgQueryTool.queryForListEntities<YourEntity>(
    query = "SELECT * FROM your_table WHERE status = ?",
    parameters = listOf("ACTIVE")
)
```

This method will:
- Execute the SQL query with the provided parameters
- Map each result row to your data class
- Return an empty list if no results are found

## Creating Entity Classes

To use `PgQueryTool`, you need to define data classes that represent your database entities. Here's an example:

```kotlin
data class UserEntity(
    @Column(name = "id")
    val id: UUID,
    
    @Column(name = "username")
    val username: String,
    
    @Column(name = "email")
    val email: String,
    
    @Column(name = "created_at")
    val createdAt: Timestamp
) {
    companion object {
        fun getByIdQuery(): String = """
            SELECT 
                id,
                username,
                email,
                created_at
            FROM users
            WHERE id = ?
        """.trimIndent()
        
        fun getAllActiveUsersQuery(): String = """
            SELECT 
                id,
                username,
                email,
                created_at
            FROM users
            WHERE status = 'ACTIVE'
        """.trimIndent()
    }
}
```

### Best Practices for Entity Classes

1. **Use Data Classes**: Kotlin data classes work best with `PgQueryTool` as they have a well-defined primary constructor.

2. **Column Annotations**: Use `@Column` annotations to explicitly map class properties to database columns. This improves code readability.

3. **Define Queries in Companion Object**: Place your SQL queries in the companion object of your entity class. This keeps your queries organized and close to the entity they relate to.

4. **Use Parameterized Queries**: Always use parameterized queries (`?` placeholders) instead of string concatenation to prevent SQL injection.

## Column Name Mapping

The `PgQueryTool` automatically converts camelCase property names to snake_case column names. For example:
- `userId` property maps to `user_id` column
- `createdAt` property maps to `created_at` column

If your database uses a different naming convention, you can use the `@Column` annotation to explicitly specify the column name.

## Example Usage

Here's a complete example of using `PgQueryTool` in an integration test:

```kotlin
@Test
fun testUserRetrieval() {
    // Arrange - insert test data into the database
    // (This might be done in a setup method or using a database migration)
    
    // Act - retrieve the user from the database
    val user = PgQueryTool.queryForEntity<UserEntity>(
        UserEntity.getByIdQuery(),
        listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    )
    
    // Assert - verify the user data
    assertThat(user).isNotNull
    assertThat(user!!.username).isEqualTo("testuser")
    assertThat(user.email).isEqualTo("test@example.com")
}
```

## Connection Management

The `PgQueryTool` automatically manages database connections:
- Connections are obtained from the `PostgresContainer` instance
- Connections are properly closed after each query using Kotlin's `use` function
- Statement and ResultSet resources are also properly closed

This ensures that your tests don't leak database connections and resources.