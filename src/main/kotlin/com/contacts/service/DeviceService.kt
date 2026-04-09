package com.contacts.service

import com.contacts.dto.device.DeviceDto
import com.contacts.exception.ForbiddenException
import com.contacts.exception.ResourceNotFoundException
import com.contacts.repository.UserDeviceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeviceService(private val userDeviceRepository: UserDeviceRepository) {

    fun getDevices(userId: UUID): List<DeviceDto> {
        return userDeviceRepository.findAllByUserId(userId).map { device ->
            DeviceDto(
                id = device.id,
                deviceId = device.deviceId,
                deviceName = device.deviceName,
                deviceType = device.deviceType,
                publicKey = device.publicKey,
                lastActiveAt = device.lastActiveAt,
                isOnline = device.isOnline,
                createdAt = device.createdAt
            )
        }
    }

    @Transactional
    fun removeDevice(userId: UUID, deviceId: String, requestingDeviceId: String) {
        val device = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId)
            .orElseThrow { ResourceNotFoundException("Device not found: $deviceId") }

        if (device.user.id != userId) {
            throw ForbiddenException("Not your device")
        }

        userDeviceRepository.delete(device)
    }
}
