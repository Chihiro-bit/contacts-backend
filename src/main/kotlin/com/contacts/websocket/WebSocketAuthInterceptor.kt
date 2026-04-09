package com.contacts.websocket

import com.contacts.security.CustomUserDetailsService
import com.contacts.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

@Component
class WebSocketAuthInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: CustomUserDetailsService
) : ChannelInterceptor {

    private val log = LoggerFactory.getLogger(WebSocketAuthInterceptor::class.java)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        if (accessor?.command == StompCommand.CONNECT) {
            val token = accessor.getFirstNativeHeader("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()

            if (token != null && jwtTokenProvider.validateToken(token)) {
                try {
                    val userId = jwtTokenProvider.getUserIdFromToken(token)
                    val deviceId = jwtTokenProvider.getDeviceIdFromToken(token)
                    val userDetails = userDetailsService.loadUserByUserId(userId, deviceId)
                    val auth = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                    accessor.user = auth
                    log.debug("WebSocket authenticated: userId={}, deviceId={}", userId, deviceId)
                } catch (ex: Exception) {
                    log.warn("WebSocket authentication failed: {}", ex.message)
                }
            } else {
                log.warn("WebSocket connection without valid token")
            }
        }

        return message
    }
}
