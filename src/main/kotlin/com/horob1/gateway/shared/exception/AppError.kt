package com.horob1.gateway.shared.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class AppError(
    val code: String,
    val message: String,
    val status: HttpStatusCode,
) {
    data object InternalServerError : AppError(
        code = "SERVER_0001",
        message = "An error occurred on the server, please try again later.",
        status = HttpStatus.INTERNAL_SERVER_ERROR,
    )

    data object NotFound : AppError(
        code = "SERVER_0002",
        message = "The resource you requested does not exist.",
        status = HttpStatus.NOT_FOUND,
    )

    data object MethodNotAllowed : AppError(
        code = "SERVER_0003",
        message = "Method not supported.",
        status = HttpStatus.METHOD_NOT_ALLOWED,
    )

    // --- Authentication & Authorization Errors ---

    data object AccessForbidden : AppError(
        code = "AUTH_0001",
        message = "You do not have permission to access this resource.",
        status = HttpStatus.FORBIDDEN,
    )

    data object AuthenticationFailed : AppError(
        code = "AUTH_0002",
        message = "Authentication failed.",
        status = HttpStatus.UNAUTHORIZED,
    )

    data object TokenExpired : AppError(
        code = "AUTH_0003",
        message = "Token has expired.",
        status = HttpStatus.UNAUTHORIZED,
    )

    data object InvalidSignature : AppError(
        code = "AUTH_0004",
        message = "Invalid token signature.",
        status = HttpStatus.UNAUTHORIZED,
    )

    data object InvalidToken : AppError(
        code = "AUTH_0005",
        message = "Invalid or malformed token.",
        status = HttpStatus.UNAUTHORIZED,
    )

    data object UnsupportedToken : AppError(
        code = "AUTH_0006",
        message = "Unsupported token.",
        status = HttpStatus.UNAUTHORIZED,
    )

    data object MissingToken : AppError(
        code = "AUTH_0007",
        message = "Missing token in request header.",
        status = HttpStatus.UNAUTHORIZED,
    )

    data object SigningKeyNotConfigured : AppError(
        code = "AUTH_0008",
        message = "Signing key not configured for token type.",
        status = HttpStatus.INTERNAL_SERVER_ERROR,
    )

    data object JwtProcessingError : AppError(
        code = "AUTH_0009",
        message = "Unexpected error while processing token.",
        status = HttpStatus.INTERNAL_SERVER_ERROR,
    )

    data object NotInternalCall : AppError(
        code = "AUTH_0010",
        message = "Invalid request.",
        status = HttpStatus.UNAUTHORIZED,
    )

    // --- Input Data Errors ---

    data object ValidationError : AppError(
        code = "VALIDATION_0001",
        message = "Invalid input data.",
        status = HttpStatus.UNPROCESSABLE_ENTITY,
    )

    class CustomError(
        code: String,
        message: String,
        status: HttpStatusCode,
    ) : AppError(code, message, status)
}