package com.contacts.controller

import com.contacts.common.ApiResponse
import com.contacts.dto.contact.ContactResponse
import com.contacts.dto.contact.ContactSyncResponse
import com.contacts.dto.contact.CreateContactRequest
import com.contacts.dto.contact.DeleteContactResponse
import com.contacts.dto.contact.UpdateContactRequest
import com.contacts.security.UserPrincipal
import com.contacts.service.ContactService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contacts")
class ContactController(private val contactService: ContactService) {

    @GetMapping
    @Operation(summary = "Get paginated contacts")
    fun getContacts(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<ContactResponse>> {
        return ApiResponse.ok(contactService.getContacts(principal.userId, page, size))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new contact")
    fun createContact(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateContactRequest
    ): ApiResponse<ContactResponse> {
        return ApiResponse.ok(contactService.createContact(principal.userId, request))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a contact")
    fun updateContact(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateContactRequest
    ): ApiResponse<ContactResponse> {
        return ApiResponse.ok(contactService.updateContact(principal.userId, id, principal.deviceId, request))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Initiate delete (5-second undo window)")
    fun deleteContact(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ApiResponse<DeleteContactResponse> {
        return ApiResponse.ok(contactService.initiateDelete(principal.userId, id, principal.deviceId))
    }

    @PostMapping("/{id}/undo-delete")
    @Operation(summary = "Undo pending delete")
    fun undoDelete(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ApiResponse<ContactResponse> {
        return ApiResponse.ok(contactService.undoDelete(principal.userId, id, principal.deviceId))
    }

    @GetMapping("/sync")
    @Operation(summary = "Incremental sync since a given version")
    fun syncContacts(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "0") sinceVersion: Long
    ): ApiResponse<ContactSyncResponse> {
        return ApiResponse.ok(contactService.syncContacts(principal.userId, sinceVersion))
    }
}
