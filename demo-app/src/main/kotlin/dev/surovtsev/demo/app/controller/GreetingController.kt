package dev.surovtsev.demo.app.controller

import dev.surovtsev.demo.app.kafka.GreetingKafkaProducer
import dev.surovtsev.demo.app.service.GreetingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class GreetingController(
    private val greetingService: GreetingService,
    private val greetingKafkaProducer: GreetingKafkaProducer
) {

    @GetMapping("/greeting")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): Map<String, String> {
        val message = greetingService.getGreeting(name)

        // Send the greeting to Kafka
        greetingKafkaProducer.sendGreeting(name)

        return mapOf("message" to message)
    }
}
