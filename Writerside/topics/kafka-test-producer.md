# Using KafkaTestProducer

The `KafkaTestProducer` is a utility for sending test messages to Kafka topics in your integration tests. It simplifies the process of producing events to Kafka, allowing you to test how your application consumes and processes these events.

## Overview

The `KafkaTestProducer` is designed to:
- Create a Kafka producer with appropriate configuration
- Send messages to specified topics with the correct headers and format
- Log successful message delivery

## How It Works

The `KafkaTestProducer` automatically connects to the Kafka instance provided by `IntegrationKafkaContainer`, which is a TestContainers instance configured for your integration tests.

## Sending Messages

To send a message to a Kafka topic, use the `sendMessages` method:

```kotlin
KafkaTestProducer.sendMessages(
    event = YourEventDefinition,
    body = jsonString,
    headers = kafkaHeaders
)
```

### Parameters

- **event**: A `KafkaEventDefinition` that specifies the topic name and event type
- **body**: The message body as a JSON string
- **headers**: Optional `KafkaHeaders` object containing metadata for the message

### KafkaHeaders

The `KafkaHeaders` object can contain:
- **actionId**: A UUID that identifies the action or transaction
- Other custom headers as needed

If you don't provide headers, a random UUID will be generated for the message key.

## Example Usage

Here's a complete example of using `KafkaTestProducer` in an integration test:

```kotlin
@Test
fun testKafkaMessageProcessing() {
    // Arrange
    val actionId = UUID.randomUUID()
    val messageBody = """
        {
            "userId": "12345",
            "action": "login",
            "timestamp": "${Instant.now()}"
        }
    """.trimIndent()
    
    val headers = KafkaHeaders(actionId = actionId)
    
    // Act - send a message to Kafka
    KafkaTestProducer.sendMessages(
        event = UserActionEvent,
        body = messageBody,
        headers = headers
    )
    
    // Assert - verify that your application processed the message correctly
    // (This might involve checking a database, calling an API, etc.)
    yourTestVerificationLogic()
}
```

## Creating Event Definitions

To use `KafkaTestProducer`, you need to define your event types. Here's an example of how to create a `KafkaEventDefinition`:

```kotlin
object UserActionEvent : KafkaEventDefinition<UserAction> {
    override val topicName: String = "user-actions"
    override val eventType: String = "user.action"
    override val clazz: Class<UserAction> = UserAction::class.java
}

data class UserAction(
    val userId: String,
    val action: String,
    val timestamp: Instant
)
```

## Headers Added Automatically

The `KafkaTestProducer` automatically adds the following headers to each message:
- The event type (from your `KafkaEventDefinition`)
- `contentType: application/json`

## Configuration

The `KafkaTestProducer` uses the following configuration:
- Bootstrap servers from `IntegrationKafkaContainer`
- A unique client ID for each producer instance
- String serializers for both keys and values

This configuration ensures that your test messages are properly formatted and delivered to the Kafka topics in your test environment.