package com.contacts.service

import com.contacts.domain.entity.OutboxEvent
import com.contacts.repository.OutboxEventRepository
import com.contacts.websocket.ContactNotificationPayload
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class EventPublishService(
    private val outboxEventRepository: OutboxEventRepository,
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(EventPublishService::class.java)

    @Transactional
    fun saveEvent(
        eventType: String,
        contactId: UUID,
        userId: UUID,
        payload: ContactNotificationPayload
    ) {
        val event = OutboxEvent(
            eventType = eventType,
            aggregateId = contactId.toString(),
            userId = userId,
            payload = objectMapper.writeValueAsString(payload)
        )
        outboxEventRepository.save(event)
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun processPendingEvents() {
        val events = outboxEventRepository.findPendingEvents()
        if (events.isEmpty()) return

        for (event in events) {
            try {
                val payload = objectMapper.readValue(event.payload, ContactNotificationPayload::class.java)
                notificationService.sendContactNotification(event.userId, payload)
                event.status = "PROCESSED"
                event.processedAt = Instant.now()
                outboxEventRepository.save(event)
            } catch (ex: Exception) {
                log.error("Failed to process outbox event {}: {}", event.id, ex.message)
                event.status = "FAILED"
                outboxEventRepository.save(event)
            }
        }
    }
}
