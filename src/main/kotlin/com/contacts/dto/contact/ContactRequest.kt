package com.contacts.dto.contact

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateContactRequest(
    @field:NotBlank
    val encryptedPayload: String,

    @field:NotBlank
    val payloadNonce: String,

    val payloadTag: String? = null,

    @field:Positive
    val keyVersion: Int = 1
)

data class UpdateContactRequest(
    @field:NotBlank
    val encryptedPayload: String,

    @field:NotBlank
    val payloadNonce: String,

    val payloadTag: String? = null,

    @field:Positive
    val keyVersion: Int = 1
)
