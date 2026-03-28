package com.horob1.gateway.config

import com.horob1.gateway.shared.enums.TokenType
import com.horob1.gateway.api.interceptor.VerifyUserFilterFactory
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayRoutes(
    private val verifyUserFilterFactory: VerifyUserFilterFactory
) {

    @Bean
    fun customRoutes(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes {
            // Route cho auth verify email
            route("auth-verify-email") {
                path("/api/v1/auth/verify-email")
                    .or(path("/api/v1/auth/send-verification-email"))
                filters {
                    filter(
                        verifyUserFilterFactory.apply(
                            VerifyUserFilterFactory.Config(tokenType = TokenType.EMAIL_VERIFY)
                        )
                    )
                }
                uri("lb://AUTH-SERVICE")
            }

            route("auth-reset-password") {
                path("/api/v1/auth/send-verification-password")
                    .or(path("/api/v1/auth/reset-password"))
                filters {
                    filter(
                        verifyUserFilterFactory.apply(
                            VerifyUserFilterFactory.Config(tokenType = TokenType.PASSWORD_RESET)
                        )
                    )
                }
                uri("lb://AUTH-SERVICE")
            }

            route("auth-2fa") {
                path("/api/v1/auth/2fa")
                filters {
                    filter(
                        verifyUserFilterFactory.apply(
                            VerifyUserFilterFactory.Config(tokenType = TokenType.TWO_FA_VERIFY)
                        )
                    )
                }
                uri("lb://AUTH-SERVICE")
            }

            route("auth-logout") {
                path("/api/v1/auth/logout")
                filters {
                    filter(
                        verifyUserFilterFactory.apply(
                            VerifyUserFilterFactory.Config(tokenType = TokenType.ACCESS)
                        )
                    )
                }
                uri("lb://AUTH-SERVICE")
            }

            // Protected auth query endpoints (must be before catch-all)
            route("auth-protected") {
                path("/api/v1/auth/roles")
                    .or(path("/api/v1/auth/devices"))
                filters {
                    filter(
                        verifyUserFilterFactory.apply(
                            VerifyUserFilterFactory.Config(tokenType = TokenType.ACCESS)
                        )
                    )
                }
                uri("lb://AUTH-SERVICE")
            }

            route("auth") {
                path("/api/v1/auth/**")
                uri("lb://AUTH-SERVICE")
            }

            // Profile endpoints (protected)
            route("profile") {
                path("/api/v1/profile/**")
                    .or(path("/api/v1/profile"))
                filters {
                    filter(
                        verifyUserFilterFactory.apply(
                            VerifyUserFilterFactory.Config(tokenType = TokenType.ACCESS)
                        )
                    )
                }
                uri("lb://AUTH-SERVICE")
            }

            // SePay webhook (no JWT filter)
            route("doc-webhook") {
                path("/api/transactions/**")
                uri("lb://DOC-SERVICE")
            }

            // Doc-Service protected routes (PHẢI đứng trước doc-public để tránh bị /** nuốt)
            route("doc-protected") {
                path("/api/v1/documents/my", "/api/v1/documents/*/download", "/api/v1/bookmarks/**", "/api/v1/purchases/**")
                filters {
                    filter(
                        verifyUserFilterFactory.apply(
                            VerifyUserFilterFactory.Config(tokenType = TokenType.ACCESS)
                        )
                    )
                }
                uri("lb://DOC-SERVICE")
            }

            // Doc-Service public routes
            route("doc-public") {
                path("/api/v1/documents", "/api/v1/documents/**", "/api/v1/schools/**", "/api/v1/departments/**")
                uri("lb://DOC-SERVICE")
            }

            // Auth-Service admin routes (roles, permissions, users)
            route("auth-admin") {
                path("/api/v1/roles/**", "/api/v1/roles", "/api/v1/permissions/**", "/api/v1/permissions", "/api/v1/users/**")
                filters {
                    filter(
                        verifyUserFilterFactory.apply(
                            VerifyUserFilterFactory.Config(tokenType = TokenType.ACCESS)
                        )
                    )
                }
                uri("lb://AUTH-SERVICE")
            }

            // Auth-Service public routes
            route("auth-public") {
                path("/api/v1/authors/**")
                uri("lb://AUTH-SERVICE")
            }
        }
    }
}
