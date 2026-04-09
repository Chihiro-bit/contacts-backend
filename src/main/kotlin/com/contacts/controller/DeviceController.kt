package com.contacts.controller

import com.contacts.common.ApiResponse
import com.contacts.dto.device.DeviceDto
import com.contacts.security.UserPrincipal
import com.contacts.service.DeviceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices")
class DeviceController(private val deviceService: DeviceService) {

    @GetMapping
    @Operation(summary = "Get all devices for current user")
    fun getDevices(@AuthenticationPrincipal principal: UserPrincipal): ApiResponse<List<DeviceDto>> {
        return ApiResponse.ok(deviceService.getDevices(principal.userId))
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Remove a device")
    fun removeDevice(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable deviceId: String
    ): ApiResponse<Nothing> {
        deviceService.removeDevice(principal.userId, deviceId, principal.deviceId)
        return ApiResponse.ok(message = "Device removed")
    }
}
