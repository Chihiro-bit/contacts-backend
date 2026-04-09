package com.contacts.websocket

import java.time.Instant
import java.util.UUID

data class ContactNotificationPayload(
    val eventType: String,
    val contactId: UUID,
    val userId: UUID,
    val operatorDeviceId: String,
    val deleteOperationId: String? = null,
    val pendingDeleteUntil: Instant? = null,
    val version: Long,
    val eventTime: Instant = Instant.now()
)
