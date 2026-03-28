package com.horob1.gateway.api.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.horob1.gateway.shared.dto.response.ApiResponse
import com.horob1.gateway.shared.exception.AppException
import com.horob1.gateway.shared.enums.TokenType
import com.horob1.gateway.util.jwt.JwtTokenManager
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class VerifyUserFilterFactory(
    private val jwtTokenManager: JwtTokenManager,
    private val objectMapper: ObjectMapper
) : AbstractGatewayFilterFactory<VerifyUserFilterFactory.Config>(Config::class.java) {

    data class Config(
        var tokenType: TokenType = TokenType.ACCESS
    )

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response

            val authHeader = request.headers.getFirst("Authorization")
            if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
                return@GatewayFilter sendError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid Authorization header",
                    "GATEWAY_0000"
                )
            }

            val token = authHeader.removePrefix("Bearer ").trim()

            try {
                val payload = jwtTokenManager.validateAndParse(token, config.tokenType)

                val mutatedRequest = request.mutate()
                    .header("X-User-Id", payload.subject)
                    .build()

                val mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build()

                chain.filter(mutatedExchange)
            } catch (e: AppException) {
                sendError(response, e.appError.status, e.appError.message, e.appError.code)
            } catch (e: Exception) {
                sendError(response, HttpStatus.UNAUTHORIZED, e.message.toString(), "GATEWAY_0001")
            }
        }
    }

    private fun sendError(
        response: org.springframework.http.server.reactive.ServerHttpResponse,
        status: HttpStatusCode,
        message: String,
        code: String
    ): Mono<Void> {
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON

        val body = ApiResponse.error<Unit>(
            status = status,
            message = message,
            code = code
        )

        val buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(body))
        return response.writeWith(Mono.just(buffer))
    }
}
