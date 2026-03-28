package com.horob1.gateway.infrastructure.repository

import com.horob1.gateway.domain.user_session.UserSession
import com.horob1.gateway.domain.user_session.UserSessionRepository
import org.springframework.stereotype.Repository

@Repository
class UserSessionRepositoryImpl(
    private val userSessionRedisRepository: UserSessionRedisRepository
) : UserSessionRepository {
    override fun findById(id: String): UserSession? {
        return userSessionRedisRepository.findById(id).orElse(null)
    }

    override fun save(userSession: UserSession) {
        userSessionRedisRepository.save(userSession)
    }

    override fun delete(id: String) {
        userSessionRedisRepository.deleteById(id)
    }
}