package com.contacts.service

import com.contacts.websocket.ContactNotificationPayload
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NotificationService(
    private val messagingTemplate: SimpMessagingTemplate
) {

    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    fun sendContactNotification(userId: UUID, payload: ContactNotificationPayload) {
        val destination = "/topic/users/$userId/contacts"
        try {
            messagingTemplate.convertAndSend(destination, payload)
            log.debug("Sent notification to {}: eventType={}", destination, payload.eventType)
        } catch (ex: Exception) {
            log.error("Failed to send WebSocket notification to userId={}: {}", userId, ex.message)
        }
    }
}
