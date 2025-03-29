package dev.surovtsev.demo.app.repository

import dev.surovtsev.demo.app.domain.ContactInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ContactInfoRepository : JpaRepository<ContactInfo, UUID> {
}