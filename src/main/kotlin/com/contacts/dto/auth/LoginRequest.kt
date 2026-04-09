package com.contacts.dto.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank
    val usernameOrEmail: String,

    @field:NotBlank
    val password: String,

    @field:NotBlank
    val deviceId: String,

    @field:NotBlank
    val deviceName: String,

    val deviceType: String = "OTHER",

    @field:NotBlank
    val publicKey: String
)
