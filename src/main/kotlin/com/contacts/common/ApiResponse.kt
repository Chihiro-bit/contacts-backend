package com.contacts.common

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val code: Int = 0,
    val message: String? = null,
    val data: T? = null,
    val timestamp: Instant = Instant.now()
) {
    companion object {
        fun <T> ok(data: T? = null, message: String? = null) = ApiResponse(
            success = true,
            data = data,
            message = message
        )

        fun error(code: Int, message: String) = ApiResponse<Nothing>(
            success = false,
            code = code,
            message = message
        )
    }
}

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean
)
