package dev.surovtsev.demo.app.kafka.domain

data class EventError(
    val error: String? = null,
    val localizedMessage: String? = null
)