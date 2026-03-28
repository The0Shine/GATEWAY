package com.horob1.gateway.shared.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val status: HttpStatusCode,
    val message: String,
    val code: String? = null,
    val data: T? = null,
    val timestamp: Instant = Instant.now()
) {
    companion object {
        fun <T> success(status: HttpStatusCode = HttpStatus.OK, data: T, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(
                status = status,
                message = message,
                data = data
            )
        }

        fun <T> success(status: HttpStatusCode = HttpStatus.OK, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(
                status = status,
                message = message
            )
        }

        fun <T> error(status: HttpStatusCode, message: String, code: String
        ): ApiResponse<T> {
            return ApiResponse(
                status = status,
                message = message,
                code = code
            )
        }
    }
}
