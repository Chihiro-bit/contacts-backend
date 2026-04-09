package com.contacts.common

object ErrorCode {
    const val UNAUTHORIZED = 1001
    const val FORBIDDEN = 1002
    const val NOT_FOUND = 1003
    const val CONFLICT = 1004
    const val VALIDATION_ERROR = 1005
    const val BUSINESS_ERROR = 1006
    const val INTERNAL_ERROR = 9999

    // Auth
    const val INVALID_CREDENTIALS = 2001
    const val TOKEN_EXPIRED = 2002
    const val TOKEN_INVALID = 2003
    const val REFRESH_TOKEN_INVALID = 2004

    // Contact
    const val CONTACT_NOT_FOUND = 3001
    const val CONTACT_NOT_OWNED = 3002
    const val CONTACT_NOT_PENDING_DELETE = 3003
    const val DELETE_WINDOW_EXPIRED = 3004
    const val OPERATION_ALREADY_PROCESSED = 3005
}
