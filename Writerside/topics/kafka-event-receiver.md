# Using KafkaEventReceiver

The `KafkaEventReceiver` is a powerful utility for awaiting and asserting Kafka events in your integration tests. It helps you verify that your application correctly produces events to Kafka topics.

## Setup

Before using `KafkaEventReceiver`, you need to configure it with a `KafkaEventDataSource`:

```kotlin
// Initialize KafkaEventReceiver with your data source
KafkaEventReceiver.kafkaEventDataSource = yourKafkaEventDataSource
```

The `KafkaEventDataSource` is responsible for finding events in Kafka topics. You'll need to implement this interface according to your application's needs.

## Key Methods

### Awaiting Any Event

To wait for any event of a specific type:

```kotlin
val event = KafkaEventReceiver.awaitAnyEvent(YourEventDefinition)
```

This method will:
- Poll for events of the specified type
- Assert that at least one event is found
- Return the first event found

### Awaiting Event by Action ID

To wait for an event with a specific action ID:

```kotlin
val actionId = UUID.randomUUID()
KafkaEventReceiver.awaitEvent(actionId, YourEventDefinition)
```

This method will:
- Poll for events of the specified type with the given action ID
- Assert that at least one event is found

### Awaiting Result Event

To wait for a result event with a specific action ID and get the event data:

```kotlin
val actionId = UUID.randomUUID()
val result = KafkaEventReceiver.awaitResultEvent(actionId, YourEventDefinition)
```

This method will:
- Poll for events of the specified type with the given action ID
- Return the first event found
- Log the event reception with timing information

### Awaiting Failure Event

To wait for a failure event with a specific action ID and error message:

```kotlin
val actionId = UUID.randomUUID()
val expectedErrorMessage = "Expected error message"
KafkaEventReceiver.awaitFailureEvent(actionId, YourErrorEventDefinition, expectedErrorMessage)
```

This method will:
- Poll for error events of the specified type with the given action ID
- Assert that at least one event is found
- Assert that the error message matches the expected message

## Configuration

The `KafkaEventReceiver` uses the following default configuration:
- Poll interval: 10 seconds
- Maximum wait time: 120 seconds (2 minutes)

These values ensure that your tests have enough time to wait for events while still failing in a reasonable time if the expected events don't arrive.

## Example Usage

Here's a complete example of using `KafkaEventReceiver` in an integration test:

```kotlin
@Test
fun testKafkaEventProcessing() {
    // Arrange
    val actionId = UUID.randomUUID()
    
    // Act - trigger some action in your application that should produce a Kafka event
    yourApplication.performAction(actionId)
    
    // Assert - wait for the event and verify its content
    val result = KafkaEventReceiver.awaitResultEvent(actionId, YourEventDefinition)
    
    // Additional assertions on the event content
    assertThat(result.someProperty).isEqualTo(expectedValue)
}
```

This pattern makes your integration tests more reliable by properly waiting for asynchronous events instead of using arbitrary sleep times.