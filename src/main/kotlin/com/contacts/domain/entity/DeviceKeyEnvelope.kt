package com.contacts.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "device_key_envelopes")
data class DeviceKeyEnvelope(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "device_id", nullable = false, length = 100)
    val deviceId: String,

    @Column(name = "encrypted_master_key", nullable = false, columnDefinition = "TEXT")
    val encryptedMasterKey: String,

    @Column(name = "key_version", nullable = false)
    val keyVersion: Int = 1,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
