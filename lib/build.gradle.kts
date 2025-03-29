import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

plugins {
    id("pl.allegro.tech.build.axion-release") version "1.18.16"
    id("com.gorylenko.gradle-git-properties") version "2.4.2"
    `java-library`
    `maven-publish`
}

version = scmVersion.version

dependencies {
    // ==== Core Spring dependencies ====
    api(libs.spring.context)            // Spring IoC container (dependency injection, bean management)
    api(libs.spring.web)                // Spring Web support
    api(libs.spring.boot.starter.web)   // Spring Boot Web starter
    api(libs.spring.kafka)              // Spring Kafka integration

    // ==== Database dependencies ====
    api(libs.spring.jdbc)               // Spring JDBC support for database interactions
    api(libs.h2database)                // H2 in-memory database
    api(libs.hikariCP)                  // HikariCP connection pool
    api(libs.jakarta.persistence)       // Jakarta Persistence API

    // ==== Spring Data ====
    api(libs.spring.data.commons)       // Spring Data common components (e.g., repository support)

    // ==== Testing dependencies ====
    implementation(libs.spring.boot.test)    // Spring Boot testing utilities
    implementation(libs.spring.test)         // Spring Test support
    implementation(libs.junit.jupiter)       // JUnit 5 testing framework API
    implementation(libs.awaitility.kotlin)   // Awaitility for Kotlin (async testing)

    // ==== TestContainers ====
    api(libs.testcontainers)            // Core Testcontainers library
    api(libs.testcontainers.kafka)      // Testcontainers support for Kafka
    api(libs.testcontainers.postgresql) // Testcontainers support for PostgreSQL

    // ==== Apache Kafka ====
    api(libs.kafka.clients)             // Kafka client for producer/consumer communication with Kafka broker

    // ==== JSON processing (Jackson) ====
    api(libs.jackson.databind)          // Core Jackson library for JSON serialization/deserialization
    api(libs.jackson.core)              // Jackson Core
    api(libs.jackson.annotations)       // Jackson Annotations
    api(libs.jackson.datatype.jsr310)   // Jackson module for Java 8+ date/time support (`java.time`)
    api(libs.jackson.module.kotlin)     // Jackson module for better Kotlin compatibility
    implementation(libs.json.simple)    // Lightweight JSON parsing library

    // ==== Kotlin Reflection ====
    implementation(libs.kotlin.reflect) // Kotlin reflection support (required for some serialization/deserialization)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar.get())
            groupId = project.group.toString()
            version = project.version.toString()
            repositories {
                mavenLocal()
            }
        }
    }
}


gitProperties {
    failOnNoGitDirectory = false
    keys = mutableListOf(
        "git.commit.id",
        "git.commit.time",
        "git.branch",
        "git.build.version",
        "git.commit.message.full",
        "git.commit.user.name",
        "git.commit.id.abbrev"
    )
}

scmVersion {
    useHighestVersion.set(true)
    branchVersionIncrementer.set(
        mapOf(
            "develop.*" to "incrementPatch",
            "hotfix.*" to listOf("incrementPrerelease", mapOf("initialPreReleaseIfNotOnPrerelease" to "fx.1")),
        )
    )

    branchVersionCreator.set(
        mapOf(
            "master" to KotlinClosure2({ v: String, s: ScmPosition -> "${v}" }),
            ".*" to KotlinClosure2({ v: String, s: ScmPosition -> "$v-${s.branch}" }),
        )
    )

    snapshotCreator.set { _: String, scmPosition: ScmPosition -> "-${scmPosition.shortRevision}" }
    tag.initialVersion.set { _: TagProperties, _: ScmPosition -> "1.0.0" }
    tag.prefix.set("v")
    tag.versionSeparator.set("")

    hooks {
        post { hookContext: HookContext ->
            println("scmVersion previousVersion: ${hookContext.previousVersion}")
            println("scmVersion releaseVersion: ${hookContext.releaseVersion}")
        }
    }
}

tasks.named("publish").configure {
    doLast {
        println("Published artifact: ${project.group}:${rootProject.name}:${project.version}")
    }
}
