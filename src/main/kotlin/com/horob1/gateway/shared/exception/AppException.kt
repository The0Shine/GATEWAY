package com.horob1.gateway.shared.exception

class AppException(
    val appError: AppError,
) : RuntimeException()