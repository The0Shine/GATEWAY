package com.horob1.gateway.domain.user_session

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable

@RedisHash(value = "user_sessions", timeToLive = 900)
data class UserSession(
    @Id val id: String,
    val permissions: List<String>,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
