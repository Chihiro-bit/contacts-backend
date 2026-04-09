package com.contacts.domain.entity

import com.contacts.domain.enums.DeviceType
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_devices")
data class UserDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "device_id", nullable = false, length = 100)
    val deviceId: String,

    @Column(name = "device_name", nullable = false, length = 100)
    val deviceName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    val deviceType: DeviceType,

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    val publicKey: String,

    @Column(name = "last_active_at", nullable = false)
    var lastActiveAt: Instant = Instant.now(),

    @Column(name = "is_online", nullable = false)
    var isOnline: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
