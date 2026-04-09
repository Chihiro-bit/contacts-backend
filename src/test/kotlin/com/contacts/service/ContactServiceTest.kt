package com.contacts.service

import com.contacts.config.AppProperties
import com.contacts.domain.entity.Contact
import com.contacts.domain.entity.User
import com.contacts.domain.enums.ContactStatus
import com.contacts.dto.contact.CreateContactRequest
import com.contacts.repository.ContactRepository
import com.contacts.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Optional
import java.util.UUID

class ContactServiceTest {

    @MockK
    lateinit var contactRepository: ContactRepository

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var eventPublishService: EventPublishService

    @MockK
    lateinit var appProperties: AppProperties

    private lateinit var contactService: ContactService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { appProperties.contact.pendingDeleteWindowSeconds } returns 5L
        contactService = ContactService(contactRepository, userRepository, eventPublishService, appProperties)
    }

    @Test
    fun `createContact should save and return contact response`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId, username = "test", email = "test@test.com", passwordHash = "hash")
        val contact = Contact(
            owner = user,
            encryptedPayload = "encrypted",
            payloadNonce = "nonce123"
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { contactRepository.save(any()) } returns contact
        every { eventPublishService.saveEvent(any(), any(), any(), any()) } just Runs

        val request = CreateContactRequest(
            encryptedPayload = "encrypted",
            payloadNonce = "nonce123"
        )

        val result = contactService.createContact(userId, request)

        assertEquals("encrypted", result.encryptedPayload)
        assertEquals(ContactStatus.ACTIVE, result.status)
        verify { contactRepository.save(any()) }
    }

    @Test
    fun `initiateDelete should set PENDING_DELETE status`() {
        val userId = UUID.randomUUID()
        val contactId = UUID.randomUUID()
        val user = User(id = userId, username = "test", email = "test@test.com", passwordHash = "hash")
        val contact = Contact(
            id = contactId,
            owner = user,
            encryptedPayload = "encrypted",
            payloadNonce = "nonce123"
        )

        every { contactRepository.findById(contactId) } returns Optional.of(contact)
        every { contactRepository.save(any()) } returns contact.apply {
            status = ContactStatus.PENDING_DELETE
            pendingDeleteUntil = Instant.now().plusSeconds(5)
            deleteOperationId = UUID.randomUUID().toString()
        }
        every { eventPublishService.saveEvent(any(), any(), any(), any()) } just Runs

        val result = contactService.initiateDelete(userId, contactId, "device-001")

        assertNotNull(result.deleteOperationId)
        assertNotNull(result.pendingDeleteUntil)
        verify { contactRepository.save(any()) }
    }
}
