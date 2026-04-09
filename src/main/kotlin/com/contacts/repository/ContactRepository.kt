package com.contacts.repository

import com.contacts.domain.entity.Contact
import com.contacts.domain.enums.ContactStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Repository
interface ContactRepository : JpaRepository<Contact, UUID> {
    fun findByIdAndOwnerIdAndStatusNot(id: UUID, ownerId: UUID, status: ContactStatus): Optional<Contact>

    @Query("SELECT c FROM Contact c WHERE c.owner.id = :userId AND c.status != 'DELETED'")
    fun findActiveByUserId(userId: UUID, pageable: Pageable): Page<Contact>

    @Query("SELECT c FROM Contact c WHERE c.owner.id = :userId AND c.version > :sinceVersion")
    fun findByUserIdAndVersionAfter(userId: UUID, sinceVersion: Long): List<Contact>

    @Query("SELECT c FROM Contact c WHERE c.status = 'PENDING_DELETE' AND c.pendingDeleteUntil < :now")
    fun findExpiredPendingDeletes(now: Instant): List<Contact>

    fun findByDeleteOperationId(deleteOperationId: String): Optional<Contact>
}
