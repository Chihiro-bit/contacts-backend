package com.contacts.security

import com.contacts.config.AppProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(private val appProperties: AppProperties) {

    private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(appProperties.jwt.secret))
    }

    fun generateAccessToken(userId: UUID, deviceId: String): String {
        val now = Date()
        val expiry = Date(now.time + appProperties.jwt.accessTokenExpiration)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("deviceId", deviceId)
            .claim("type", "access")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (ex: Exception) {
            log.debug("JWT validation failed: {}", ex.message)
            false
        }
    }

    fun getUserIdFromToken(token: String): UUID {
        val claims = getClaims(token)
        return UUID.fromString(claims.subject)
    }

    fun getDeviceIdFromToken(token: String): String {
        val claims = getClaims(token)
        return claims["deviceId"] as String
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
