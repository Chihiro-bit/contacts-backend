package com.contacts.dto.auth

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String,

    @field:NotBlank
    val deviceId: String
)
