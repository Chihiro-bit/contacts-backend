package com.contacts.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank @field:Size(min = 3, max = 50)
    val username: String,

    @field:NotBlank @field:Email
    val email: String,

    @field:NotBlank @field:Size(min = 8, max = 100)
    val password: String,

    @field:NotBlank
    val deviceId: String,

    @field:NotBlank
    val deviceName: String,

    val deviceType: String = "OTHER",

    @field:NotBlank
    val publicKey: String
)
