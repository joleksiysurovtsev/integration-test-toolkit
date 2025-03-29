package dev.surovtsev.demo.app.kafka.domain

data class EventMessage<T>(
    val actionId: String,
    val parentActionId: String? = null,
    val messageOriginator: String? = null,
    val actionType: String,
    val payload: T,
)


