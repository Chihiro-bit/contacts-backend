package com.contacts.repository

import com.contacts.domain.entity.DeviceKeyEnvelope
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface DeviceKeyEnvelopeRepository : JpaRepository<DeviceKeyEnvelope, UUID> {
    fun findByUserIdAndDeviceId(userId: UUID, deviceId: String): Optional<DeviceKeyEnvelope>
    fun findAllByUserId(userId: UUID): List<DeviceKeyEnvelope>
}
