package com.contacts.dto.contact

import com.contacts.domain.enums.ContactStatus
import java.time.Instant
import java.util.UUID

data class ContactResponse(
    val id: UUID,
    val encryptedPayload: String,
    val payloadNonce: String,
    val payloadTag: String?,
    val keyVersion: Int,
    val status: ContactStatus,
    val pendingDeleteUntil: Instant?,
    val deleteOperationId: String?,
    val version: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?
)

data class ContactSyncResponse(
    val contacts: List<ContactResponse>,
    val maxVersion: Long
)

data class DeleteContactResponse(
    val contactId: UUID,
    val deleteOperationId: String,
    val pendingDeleteUntil: Instant,
    val message: String = "Contact scheduled for deletion. You have 5 seconds to undo."
)
