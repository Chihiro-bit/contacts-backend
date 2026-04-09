package com.contacts.dto.auth

import java.util.UUID

data class AuthResponse(
    val userId: UUID,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)
