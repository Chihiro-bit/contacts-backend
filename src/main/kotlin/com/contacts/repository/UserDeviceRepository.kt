package com.contacts.repository

import com.contacts.domain.entity.UserDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface UserDeviceRepository : JpaRepository<UserDevice, UUID> {
    fun findByUserIdAndDeviceId(userId: UUID, deviceId: String): Optional<UserDevice>
    fun findAllByUserId(userId: UUID): List<UserDevice>
    fun findByDeviceId(deviceId: String): Optional<UserDevice>

    @Query("SELECT d FROM UserDevice d WHERE d.user.id = :userId AND d.deviceId != :excludeDeviceId AND d.isOnline = true")
    fun findOnlineDevicesByUserIdExcluding(userId: UUID, excludeDeviceId: String): List<UserDevice>

    @Modifying
    @Query("UPDATE UserDevice d SET d.isOnline = false WHERE d.user.id = :userId AND d.deviceId = :deviceId")
    fun setOffline(userId: UUID, deviceId: String): Int
}
