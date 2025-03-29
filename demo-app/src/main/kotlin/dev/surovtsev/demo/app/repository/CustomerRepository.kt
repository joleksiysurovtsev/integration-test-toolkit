package dev.surovtsev.demo.app.repository

import dev.surovtsev.demo.app.domain.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CustomerRepository : JpaRepository<Customer, UUID> {
    @Query("SELECT c FROM Customer c WHERE c.deleted = false")
    fun findAllActive(): List<Customer>

    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.deleted = false")
    fun findActiveById(id: UUID): Optional<Customer>
}
