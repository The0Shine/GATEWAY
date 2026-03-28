package com.horob1.gateway.api.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.horob1.gateway.shared.dto.response.ApiResponse
import com.horob1.gateway.shared.constant.SecurityConstants.INTERNAL_CALL_HEADER
import com.horob1.gateway.shared.constant.SecurityConstants.USER_ID_HEADER
import com.horob1.gateway.shared.constant.SecurityConstants.USER_PERMISSIONS_HEADER
import com.horob1.gateway.shared.enums.TokenType
import com.horob1.gateway.domain.user_session.UserSession
import com.horob1.gateway.domain.user_session.UserSessionRepository
import com.horob1.gateway.util.jwt.JwtTokenManager
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.Base64

@Component
class VerifyUserPermissionFilter(
    private val jwtTokenManager: JwtTokenManager,
    private val objectMapper: ObjectMapper,
    private val webClient: WebClient,
    private val userSessionRepository: UserSessionRepository
) : GatewayFilter {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response

        val authHeader = request.headers.getFirst("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            return sendError(
                response,
                "Missing or invalid Authorization header",
                "GATEWAY_0000"
            )
        }

        val token = authHeader.removePrefix("Bearer ").trim()
        val payload = try {
            jwtTokenManager.validateAndParse(token, TokenType.ACCESS)
        } catch (e: Exception) {
            return sendError(response, e.message ?: "Invalid token", "GATEWAY_0001")
        }

        val userId = payload.subject
        return Mono.fromCallable {
            userSessionRepository.findById(userId)
        }.flatMap { userSession ->
            if (userSession == null) {
                webClient.get()
                    .uri("lb://auth-service/api/v1/auth-service/$userId/permissions")
                    .header(INTERNAL_CALL_HEADER, "true")
                    .retrieve()
                    .bodyToMono(object : ParameterizedTypeReference<List<String>>() {})
                    .flatMap { permissions ->
                        userSessionRepository.save(UserSession(userId, permissions))
                        val permissionsJson = Base64.getEncoder()
                            .encodeToString(objectMapper.writeValueAsBytes(permissions))
                        val mutatedRequest = request.mutate()
                            .header(USER_ID_HEADER, userId)
                            .header(USER_PERMISSIONS_HEADER, permissionsJson)
                            .build()
                        chain.filter(exchange.mutate().request(mutatedRequest).build())
                    }
                    .onErrorResume {
                        chain.filter(exchange)
                    }
            } else {
                val permissionsJson = Base64.getEncoder()
                    .encodeToString(objectMapper.writeValueAsBytes(userSession.permissions))
                val mutatedRequest = request.mutate()
                    .header(USER_ID_HEADER, userId)
                    .header(USER_PERMISSIONS_HEADER, permissionsJson)
                    .build()
                chain.filter(exchange.mutate().request(mutatedRequest).build())
            }
        }.subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
    }

    private fun sendError(
        response: ServerHttpResponse,
        message: String,
        code: String
    ): Mono<Void> {
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = org.springframework.http.MediaType.APPLICATION_JSON

        val body = ApiResponse.error<Unit>(
            status = HttpStatus.UNAUTHORIZED,
            message = message,
            code = code
        )

        val buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(body))
        return response.writeWith(Mono.just(buffer))
    }
}
