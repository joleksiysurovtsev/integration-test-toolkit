package dev.surovtsev.demo.app.service

import dev.surovtsev.demo.app.domain.Customer
import dev.surovtsev.demo.app.repository.CustomerRepository
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

@Service
class CustomerService(private val customerRepository: CustomerRepository) {

    fun create(customer: Customer): Customer {
        return customerRepository.save(customer)
    }

    fun getById(id: UUID): Customer?{
        return customerRepository.findActiveById(id).orElse(null)
    }

    fun getAll(): List<Customer>{
        return customerRepository.findAllActive()
    }

    /**
     * Soft deletes a customer by setting the deleted flag to true
     */
    fun deleteById(id: UUID): Boolean {
        val customer = customerRepository.findById(id).orElse(null) ?: return false

        // Create a new customer with the deleted flag set to true and updateDate set to now
        val deletedCustomer = customer.copy(
            deleted = true,
            updateDate = Timestamp.valueOf(LocalDateTime.now())
        )

        customerRepository.save(deletedCustomer)
        return true
    }
}
