package com.contacts.exception

open class BusinessException(
    val code: Int,
    message: String,
    val httpStatus: Int = 400
) : RuntimeException(message)

class ResourceNotFoundException(message: String) : BusinessException(
    code = com.contacts.common.ErrorCode.NOT_FOUND,
    message = message,
    httpStatus = 404
)

class UnauthorizedException(message: String = "Unauthorized") : BusinessException(
    code = com.contacts.common.ErrorCode.UNAUTHORIZED,
    message = message,
    httpStatus = 401
)

class ForbiddenException(message: String = "Forbidden") : BusinessException(
    code = com.contacts.common.ErrorCode.FORBIDDEN,
    message = message,
    httpStatus = 403
)
