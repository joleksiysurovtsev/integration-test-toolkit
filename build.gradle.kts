import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.springframework.boot") version libs.versions.spring.boot.ver.get() apply false
}

// Common configuration for all projects
allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

// Common configuration for all subprojects
subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.spring.dependency-management")
    }

    group = "dev.surovtsev.${project.name}"
    version = "0.0.1-SNAPSHOT"

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
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
}
