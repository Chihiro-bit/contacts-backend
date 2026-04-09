package com.contacts.domain.entity

import com.contacts.domain.enums.ContactStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "contacts")
data class Contact(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    val owner: User,

    @Column(name = "encrypted_payload", nullable = false, columnDefinition = "TEXT")
    var encryptedPayload: String,

    @Column(name = "payload_nonce", nullable = false, length = 64)
    var payloadNonce: String,

    @Column(name = "payload_tag", length = 64)
    var payloadTag: String? = null,

    @Column(name = "key_version", nullable = false)
    var keyVersion: Int = 1,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    var status: ContactStatus = ContactStatus.ACTIVE,

    @Column(name = "pending_delete_until")
    var pendingDeleteUntil: Instant? = null,

    @Column(name = "delete_operation_id", length = 100)
    var deleteOperationId: String? = null,

    @Version
    @Column(nullable = false)
    var version: Long = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)
