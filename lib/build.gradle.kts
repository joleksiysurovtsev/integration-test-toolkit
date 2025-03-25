import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("pl.allegro.tech.build.axion-release") version "1.18.16"
    id("com.gorylenko.gradle-git-properties") version "2.4.2"
    id("org.springframework.boot") version libs.versions.spring.boot.ver.get() apply false
    id("io.spring.dependency-management") version "1.1.7"
    `java-library`
    `maven-publish`
}

group = "dev.surovtsev.integration-test-toolkit"
version = scmVersion.version

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // ==== Core Spring dependencies ====
    api(libs.spring.context)            // Spring IoC container (dependency injection, bean management)
    api(libs.spring.web)
    api(libs.spring.boot.starter.web)
    api(libs.spring.boot.test)          // Spring Boot testing utilities (includes JUnit, Mockito, etc.)
    api(libs.spring.jdbc)               // Spring JDBC support for database interactions
    api(libs.spring.kafka)              // Spring Kafka integration

    // ==== Spring test & data dependencies ====
    api(libs.spring.test)
    api(libs.spring.data.commons)       // Spring Data common components (e.g., repository support)

    // ==== TestContainers ====
    api(libs.testcontainers)            // Core Testcontainers library
    api(libs.testcontainers.kafka)      // Testcontainers support for Kafka
    api(libs.testcontainers.postgresql) // Testcontainers support for PostgreSQL

    // ==== JUnit ====
    api(libs.junit.jupiter)             // JUnit 5 testing framework API

    // ==== Apache Kafka ====
    api(libs.kafka.clients)             // Kafka client for producer/consumer communication with Kafka broker
    api(libs.h2database)
    api(libs.hikariCP)

    // ==== JSON processing (Jackson) ====
    api(libs.jackson.databind)          // Core Jackson library for JSON serialization/deserialization
    api(libs.jackson.datatype.jsr310)   // Jackson module for Java 8+ date/time support (`java.time`)
    api(libs.jackson.module.kotlin)     // Jackson module for better Kotlin compatibility
    api(libs.json.simple)               // Lightweight JSON parsing library

    // ==== Kotlin Reflection ====
    api(libs.kotlin.reflect)            // Kotlin reflection support (required for some serialization/deserialization)
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