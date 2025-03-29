package dev.surovtsev.demo.app.domain

import jakarta.persistence.*
import org.springframework.web.bind.annotation.DeleteMapping
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.List

@Entity
@Table(name = "customer")
data class Customer(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name")
    val name: String? = null,

    @Column(name = "age")
    val age: Int? = null,

    @OneToMany(mappedBy = "customer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val address: List<ContactInfo>? = null,

    @Column(name = "create_date")
    val createDate: Timestamp = Timestamp.valueOf(LocalDateTime.now()),

    @Column(name = "update_date")
    val updateDate: Timestamp? = null,

    @Column(name = "deleted")
    val deleted: Boolean = false
)
