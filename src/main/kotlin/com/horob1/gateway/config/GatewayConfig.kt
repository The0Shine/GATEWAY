package com.horob1.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class GatewayConfig {

    @Bean
    fun webClient(builder: WebClient.Builder): WebClient =
        builder.build()
}
