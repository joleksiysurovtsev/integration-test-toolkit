# Integration Test Toolkit

The Integration Test Toolkit is a comprehensive library designed to simplify and streamline integration testing for applications that use Kafka and PostgreSQL. It provides a set of tools and utilities that make it easier to write reliable, maintainable integration tests.

## Purpose

Integration testing is a critical part of ensuring that your application works correctly with external systems and services. However, setting up and managing test environments for integration testing can be complex and time-consuming. The Integration Test Toolkit addresses this challenge by providing:

- Containerized test environments using TestContainers
- Utilities for interacting with Kafka and PostgreSQL in tests
- Tools for asserting and verifying expected behavior

## Key Features

### Kafka Testing Support

The toolkit provides robust support for testing Kafka-based applications:

- **KafkaEventReceiver**: Allows you to await and assert Kafka events in your tests
- **KafkaTestProducer**: Simplifies sending test messages to Kafka topics

### PostgreSQL Testing Support

For database testing, the toolkit offers:

- **PgQueryTool**: A lightweight SQL utility that simplifies direct database access in tests and maps PostgreSQL rows to Kotlin data classes

### TestContainers Integration

The toolkit leverages TestContainers to provide:

- Isolated, containerized environments for your tests
- Automatic setup and teardown of test dependencies
- Consistent test execution across different environments

## Benefits

- **Simplified Testing**: Reduce boilerplate code and focus on writing meaningful tests
- **Improved Reliability**: Ensure consistent test environments and reduce flaky tests
- **Better Maintainability**: Standardize testing patterns across your codebase
- **Faster Development**: Speed up the testing process with ready-to-use utilities

## Getting Started

To get started with the Integration Test Toolkit, check out the following guides:

- [Using KafkaEventReceiver](kafka-event-receiver.md)
- [Using KafkaTestProducer](kafka-test-producer.md)
- [Using PgQueryTool](pg-query-tool.md)