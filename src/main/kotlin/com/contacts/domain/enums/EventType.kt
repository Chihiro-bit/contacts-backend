package com.contacts.domain.enums

object EventType {
    const val CONTACT_CREATED = "contact.created"
    const val CONTACT_UPDATED = "contact.updated"
    const val CONTACT_DELETE_PENDING = "contact.delete.pending"
    const val CONTACT_DELETE_REVERTED = "contact.delete.reverted"
    const val CONTACT_DELETE_CONFIRMED = "contact.delete.confirmed"
}
