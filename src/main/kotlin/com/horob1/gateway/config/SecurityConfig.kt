package com.horob1.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .headers { headers ->
                headers.xssProtection { }
                headers.contentTypeOptions { }
                headers.frameOptions {
                    XFrameOptionsHeaderWriter.XFrameOptionsMode.DENY
                }
            }
            .authorizeExchange { exchanges ->
                exchanges.pathMatchers("/actuator/**").permitAll()
                exchanges.pathMatchers("/fallback/**").permitAll()
                exchanges.pathMatchers("/**").permitAll()
            }
            .cors { }
            .build()
    }
}