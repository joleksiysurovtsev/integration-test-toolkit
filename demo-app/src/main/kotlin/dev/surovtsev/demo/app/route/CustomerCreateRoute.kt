package dev.surovtsev.demo.app.route

import dev.surovtsev.demo.app.domain.Customer
import dev.surovtsev.demo.app.kafka.domain.EventMessage
import dev.surovtsev.demo.app.kafka.domain.SampleTopicRoute
import org.springframework.stereotype.Component

@Component
class CustomerCreateRoute : SampleTopicRoute<Customer, Customer> {

    override val payloadClass = Customer::class.java

    override fun supports(actionType: String): Boolean = actionType == "CUSTOMER_CREATE"

    override fun apply(event: EventMessage<Customer>): Customer {
        return event.payload
    }
}