# Demo Application for Integration Test Toolkit

This is a simple Spring Boot application that demonstrates how to use the Integration Test Toolkit for testing.

## Overview

The demo application provides:

- A simple REST API for greetings
- Kafka message production
- Integration tests using the Integration Test Toolkit

## Components

- `DemoApplication`: The main Spring Boot application class
- `GreetingController`: A REST controller that provides a greeting endpoint
- `GreetingService`: A service that generates greeting messages
- `GreetingKafkaProducer`: A Kafka producer that sends greeting messages to Kafka

## Integration Tests

The demo application includes integration tests that demonstrate how to use the Integration Test Toolkit:

- `GreetingControllerIntegrationTest`: Tests the REST controller using MockMvc
- `GreetingKafkaIntegrationTest`: Tests the Kafka producer using the Integration Test Toolkit's Kafka support

## Running the Application

To run the application:

```bash
./gradlew :demo-app:bootRun
```

## Running the Tests

To run the tests:

```bash
./gradlew :demo-app:test
```

To run the tests with Kafka enabled:

```bash
./gradlew :demo-app:test -Dtest.kafka.enabled=true
```

## API Endpoints

- `GET /greeting`: Returns a greeting message
  - Query parameter: `name` (optional, defaults to "World")
  - Example: `GET /greeting?name=John`
  - Response: `{"message": "Hello, John!"}`