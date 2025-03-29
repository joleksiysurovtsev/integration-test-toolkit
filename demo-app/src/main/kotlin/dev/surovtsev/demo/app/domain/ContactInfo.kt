package dev.surovtsev.demo.app.domain

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "contacts")
data class ContactInfo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "street")
    val street: String,

    @Column(name = "city")
    val city: String,

    @Column(name = "zip_code")
    val zipCode: String,

    @Column(name = "country")
    val country: String,

    @Column(name = "phone_number")
    val phoneNumber: String,

    @Column(name = "email")
    val email: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    val customer: Customer? = null,

    @Column(name = "type")
    val type: AddressType = AddressType.MAIN
)