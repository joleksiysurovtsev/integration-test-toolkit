plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Local dependency on the integration-test-toolkit
    implementation(project(":lib"))

    // Spring Boot dependencies
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.data.jpa)

    // Kafka dependencies
    implementation(libs.spring.kafka)

    // Database dependencies
    implementation(libs.spring.jdbc)
    implementation(libs.h2database)
    implementation(libs.kotlinx.coroutines.core)

    // JSON processing
    implementation(libs.jackson.databind)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)

    // Database
    implementation(libs.postgresql)
    implementation(libs.liquibase.core)

    // Test dependencies
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.junit.jupiter)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
