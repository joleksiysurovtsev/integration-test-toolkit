package dev.surovtsev.demo.app.kafka.domain

interface SampleTopicRoute<T, S> {
    val payloadClass: Class<T>
    fun supports(actionType: String): Boolean
    fun apply(event: EventMessage<T>): S
}