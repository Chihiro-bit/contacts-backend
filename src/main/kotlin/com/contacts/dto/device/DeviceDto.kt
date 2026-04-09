package com.contacts.dto.device

import com.contacts.domain.enums.DeviceType
import java.time.Instant
import java.util.UUID

data class DeviceDto(
    val id: UUID,
    val deviceId: String,
    val deviceName: String,
    val deviceType: DeviceType,
    val publicKey: String,
    val lastActiveAt: Instant,
    val isOnline: Boolean,
    val createdAt: Instant
)
