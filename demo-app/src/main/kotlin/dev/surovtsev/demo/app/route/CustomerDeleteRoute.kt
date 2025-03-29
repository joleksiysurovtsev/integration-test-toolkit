package dev.surovtsev.demo.app.route

import dev.surovtsev.demo.app.kafka.domain.EventMessage
import dev.surovtsev.demo.app.kafka.domain.SampleTopicRoute
import dev.surovtsev.demo.app.service.CustomerService
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomerDeleteRoute(private val customerService: CustomerService) : SampleTopicRoute<UUID, Boolean> {

    override val payloadClass = UUID::class.java

    override fun supports(actionType: String): Boolean = actionType == "CUSTOMER_DELETE"

    override fun apply(event: EventMessage<UUID>): Boolean {
        val customerId = event.payload
        return customerService.deleteById(customerId)
    }
}