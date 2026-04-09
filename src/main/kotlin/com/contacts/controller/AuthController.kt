package com.contacts.controller

import com.contacts.common.ApiResponse
import com.contacts.dto.auth.AuthResponse
import com.contacts.dto.auth.LoginRequest
import com.contacts.dto.auth.RefreshTokenRequest
import com.contacts.dto.auth.RegisterRequest
import com.contacts.security.UserPrincipal
import com.contacts.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    fun register(@Valid @RequestBody request: RegisterRequest): ApiResponse<AuthResponse> {
        return ApiResponse.ok(authService.register(request), "User registered successfully")
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<AuthResponse> {
        return ApiResponse.ok(authService.login(request))
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ApiResponse<AuthResponse> {
        return ApiResponse.ok(authService.refreshToken(request))
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current device")
    fun logout(@AuthenticationPrincipal principal: UserPrincipal): ApiResponse<Nothing> {
        authService.logout(principal.userId, principal.deviceId)
        return ApiResponse.ok(message = "Logged out successfully")
    }
}
