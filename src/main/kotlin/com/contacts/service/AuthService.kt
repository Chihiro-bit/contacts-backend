package com.contacts.service

import com.contacts.common.ErrorCode
import com.contacts.config.AppProperties
import com.contacts.domain.entity.RefreshToken
import com.contacts.domain.entity.User
import com.contacts.domain.entity.UserDevice
import com.contacts.domain.enums.DeviceType
import com.contacts.dto.auth.AuthResponse
import com.contacts.dto.auth.LoginRequest
import com.contacts.dto.auth.RefreshTokenRequest
import com.contacts.dto.auth.RegisterRequest
import com.contacts.exception.BusinessException
import com.contacts.exception.UnauthorizedException
import com.contacts.repository.RefreshTokenRepository
import com.contacts.repository.UserDeviceRepository
import com.contacts.repository.UserRepository
import com.contacts.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userDeviceRepository: UserDeviceRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val appProperties: AppProperties
) {

    private val log = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw BusinessException(ErrorCode.CONFLICT, "Username already taken", 409)
        }
        if (userRepository.existsByEmail(request.email)) {
            throw BusinessException(ErrorCode.CONFLICT, "Email already registered", 409)
        }
        val user = userRepository.save(
            User(
                username = request.username,
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password)
            )
        )

        val deviceType = runCatching { DeviceType.valueOf(request.deviceType.uppercase()) }
            .getOrDefault(DeviceType.OTHER)

        userDeviceRepository.save(
            UserDevice(
                user = user,
                deviceId = request.deviceId,
                deviceName = request.deviceName,
                deviceType = deviceType,
                publicKey = request.publicKey,
                isOnline = true
            )
        )

        return generateTokens(user, request.deviceId)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.usernameOrEmail)
            .orElseGet { userRepository.findByEmail(request.usernameOrEmail).orElse(null) }
            ?: throw UnauthorizedException("Invalid credentials")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid credentials")
        }

        val deviceType = runCatching { DeviceType.valueOf(request.deviceType.uppercase()) }
            .getOrDefault(DeviceType.OTHER)

        val existingDevice = userDeviceRepository.findByUserIdAndDeviceId(user.id, request.deviceId)
        if (existingDevice.isPresent) {
            val device = existingDevice.get()
            device.isOnline = true
            device.lastActiveAt = Instant.now()
            userDeviceRepository.save(device)
        } else {
            userDeviceRepository.save(
                UserDevice(
                    user = user,
                    deviceId = request.deviceId,
                    deviceName = request.deviceName,
                    deviceType = deviceType,
                    publicKey = request.publicKey,
                    isOnline = true
                )
            )
        }

        // Revoke previous refresh tokens for this device
        refreshTokenRepository.revokeByUserIdAndDeviceId(user.id, request.deviceId)

        return generateTokens(user, request.deviceId)
    }

    @Transactional
    fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val tokenHash = hashToken(request.refreshToken)
        val refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { BusinessException(ErrorCode.REFRESH_TOKEN_INVALID, "Invalid refresh token", 401) }

        if (refreshToken.revoked || refreshToken.expiresAt.isBefore(Instant.now())) {
            throw BusinessException(ErrorCode.REFRESH_TOKEN_INVALID, "Refresh token expired or revoked", 401)
        }

        if (refreshToken.deviceId != request.deviceId) {
            throw BusinessException(ErrorCode.REFRESH_TOKEN_INVALID, "Device mismatch", 401)
        }

        refreshToken.revoked = true
        refreshTokenRepository.save(refreshToken)

        return generateTokens(refreshToken.user, request.deviceId)
    }

    @Transactional
    fun logout(userId: UUID, deviceId: String) {
        refreshTokenRepository.revokeByUserIdAndDeviceId(userId, deviceId)
        userDeviceRepository.setOffline(userId, deviceId)
    }

    private fun generateTokens(user: User, deviceId: String): AuthResponse {
        val accessToken = jwtTokenProvider.generateAccessToken(user.id, deviceId)
        val rawRefreshToken = UUID.randomUUID().toString()
        val tokenHash = hashToken(rawRefreshToken)

        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                deviceId = deviceId,
                tokenHash = tokenHash,
                expiresAt = Instant.now().plusMillis(appProperties.jwt.refreshTokenExpiration)
            )
        )

        return AuthResponse(
            userId = user.id,
            username = user.username,
            accessToken = accessToken,
            refreshToken = rawRefreshToken
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }
}
