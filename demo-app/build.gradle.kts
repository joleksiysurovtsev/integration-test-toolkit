import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("org.springframework.boot") version libs.versions.spring.boot.ver.get()
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.surovtsev.demo-app"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Local dependency on the integration-test-toolkit
    implementation(project(":lib"))

    // Spring Boot dependencies
    implementation(libs.spring.boot.starter.web)
    
    // Kafka dependencies
    implementation(libs.spring.kafka)
    
    // Database dependencies
    implementation(libs.spring.jdbc)
    implementation(libs.h2database)
    
    // Test dependencies
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.junit.jupiter)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}