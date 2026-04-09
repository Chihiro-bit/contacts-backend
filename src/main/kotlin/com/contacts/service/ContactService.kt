package com.contacts.service

import com.contacts.common.ErrorCode
import com.contacts.config.AppProperties
import com.contacts.domain.entity.Contact
import com.contacts.domain.enums.ContactStatus
import com.contacts.domain.enums.EventType
import com.contacts.dto.contact.ContactResponse
import com.contacts.dto.contact.ContactSyncResponse
import com.contacts.dto.contact.CreateContactRequest
import com.contacts.dto.contact.DeleteContactResponse
import com.contacts.dto.contact.UpdateContactRequest
import com.contacts.exception.BusinessException
import com.contacts.exception.ForbiddenException
import com.contacts.exception.ResourceNotFoundException
import com.contacts.repository.ContactRepository
import com.contacts.repository.UserRepository
import com.contacts.websocket.ContactNotificationPayload
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ContactService(
    private val contactRepository: ContactRepository,
    private val userRepository: UserRepository,
    private val eventPublishService: EventPublishService,
    private val appProperties: AppProperties
) {

    private val log = LoggerFactory.getLogger(ContactService::class.java)

    @Transactional
    fun createContact(userId: UUID, request: CreateContactRequest): ContactResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val contact = contactRepository.save(
            Contact(
                owner = user,
                encryptedPayload = request.encryptedPayload,
                payloadNonce = request.payloadNonce,
                payloadTag = request.payloadTag,
                keyVersion = request.keyVersion
            )
        )

        val payload = ContactNotificationPayload(
            eventType = EventType.CONTACT_CREATED,
            contactId = contact.id,
            userId = userId,
            operatorDeviceId = "",
            version = contact.version
        )
        eventPublishService.saveEvent(EventType.CONTACT_CREATED, contact.id, userId, payload)

        return contact.toResponse()
    }

    @Transactional
    fun updateContact(userId: UUID, contactId: UUID, deviceId: String, request: UpdateContactRequest): ContactResponse {
        val contact = findContactForUser(userId, contactId)

        contact.encryptedPayload = request.encryptedPayload
        contact.payloadNonce = request.payloadNonce
        contact.payloadTag = request.payloadTag
        contact.keyVersion = request.keyVersion
        contact.updatedAt = Instant.now()

        val saved = contactRepository.save(contact)

        val payload = ContactNotificationPayload(
            eventType = EventType.CONTACT_UPDATED,
            contactId = contact.id,
            userId = userId,
            operatorDeviceId = deviceId,
            version = saved.version
        )
        eventPublishService.saveEvent(EventType.CONTACT_UPDATED, contact.id, userId, payload)

        return saved.toResponse()
    }

    @Transactional
    fun initiateDelete(userId: UUID, contactId: UUID, deviceId: String): DeleteContactResponse {
        val contact = findContactForUser(userId, contactId)

        val deleteOperationId = UUID.randomUUID().toString()
        val pendingDeleteUntil = Instant.now().plusSeconds(appProperties.contact.pendingDeleteWindowSeconds)

        contact.status = ContactStatus.PENDING_DELETE
        contact.pendingDeleteUntil = pendingDeleteUntil
        contact.deleteOperationId = deleteOperationId
        contact.updatedAt = Instant.now()

        val saved = contactRepository.save(contact)

        val payload = ContactNotificationPayload(
            eventType = EventType.CONTACT_DELETE_PENDING,
            contactId = contact.id,
            userId = userId,
            operatorDeviceId = deviceId,
            deleteOperationId = deleteOperationId,
            pendingDeleteUntil = pendingDeleteUntil,
            version = saved.version
        )
        eventPublishService.saveEvent(EventType.CONTACT_DELETE_PENDING, contact.id, userId, payload)

        return DeleteContactResponse(
            contactId = contact.id,
            deleteOperationId = deleteOperationId,
            pendingDeleteUntil = pendingDeleteUntil
        )
    }

    @Transactional
    fun undoDelete(userId: UUID, contactId: UUID, deviceId: String): ContactResponse {
        val contact = contactRepository.findById(contactId)
            .orElseThrow { ResourceNotFoundException("Contact not found") }

        if (contact.owner.id != userId) {
            throw ForbiddenException("Not your contact")
        }

        if (contact.status != ContactStatus.PENDING_DELETE) {
            throw BusinessException(ErrorCode.CONTACT_NOT_PENDING_DELETE, "Contact is not in pending delete state", 409)
        }

        if (contact.pendingDeleteUntil?.isBefore(Instant.now()) == true) {
            throw BusinessException(ErrorCode.DELETE_WINDOW_EXPIRED, "Delete window has expired", 409)
        }

        val operationId = contact.deleteOperationId
        contact.status = ContactStatus.ACTIVE
        contact.pendingDeleteUntil = null
        contact.deleteOperationId = null
        contact.updatedAt = Instant.now()

        val saved = contactRepository.save(contact)

        val payload = ContactNotificationPayload(
            eventType = EventType.CONTACT_DELETE_REVERTED,
            contactId = contact.id,
            userId = userId,
            operatorDeviceId = deviceId,
            deleteOperationId = operationId,
            version = saved.version
        )
        eventPublishService.saveEvent(EventType.CONTACT_DELETE_REVERTED, contact.id, userId, payload)

        return saved.toResponse()
    }

    fun getContacts(userId: UUID, page: Int, size: Int): List<ContactResponse> {
        val pageable = PageRequest.of(page, size)
        return contactRepository.findActiveByUserId(userId, pageable).content.map { it.toResponse() }
    }

    fun syncContacts(userId: UUID, sinceVersion: Long): ContactSyncResponse {
        val contacts = contactRepository.findByUserIdAndVersionAfter(userId, sinceVersion)
        val maxVersion = contacts.maxOfOrNull { it.version } ?: sinceVersion
        return ContactSyncResponse(
            contacts = contacts.map { it.toResponse() },
            maxVersion = maxVersion
        )
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    fun confirmPendingDeletes() {
        val expired = contactRepository.findExpiredPendingDeletes(Instant.now())
        for (contact in expired) {
            try {
                val operationId = contact.deleteOperationId
                contact.status = ContactStatus.DELETED
                contact.deletedAt = Instant.now()
                contact.updatedAt = Instant.now()
                contact.pendingDeleteUntil = null

                val saved = contactRepository.save(contact)

                val payload = ContactNotificationPayload(
                    eventType = EventType.CONTACT_DELETE_CONFIRMED,
                    contactId = contact.id,
                    userId = contact.owner.id,
                    operatorDeviceId = "",
                    deleteOperationId = operationId,
                    version = saved.version
                )
                eventPublishService.saveEvent(
                    EventType.CONTACT_DELETE_CONFIRMED,
                    contact.id,
                    contact.owner.id,
                    payload
                )
                log.info("Confirmed delete for contact {}", contact.id)
            } catch (ex: Exception) {
                log.error("Failed to confirm delete for contact {}: {}", contact.id, ex.message)
            }
        }
    }

    private fun findContactForUser(userId: UUID, contactId: UUID): Contact {
        val contact = contactRepository.findById(contactId)
            .orElseThrow { ResourceNotFoundException("Contact not found") }

        if (contact.owner.id != userId) {
            throw ForbiddenException("Not your contact")
        }

        if (contact.status == ContactStatus.DELETED) {
            throw ResourceNotFoundException("Contact not found")
        }

        return contact
    }

    private fun Contact.toResponse() = ContactResponse(
        id = id,
        encryptedPayload = encryptedPayload,
        payloadNonce = payloadNonce,
        payloadTag = payloadTag,
        keyVersion = keyVersion,
        status = status,
        pendingDeleteUntil = pendingDeleteUntil,
        deleteOperationId = deleteOperationId,
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}
