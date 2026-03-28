package com.horob1.gateway.domain.user_session

interface UserSessionRepository {
    fun findById(id: String): UserSession?

    fun save(userSession: UserSession)

    fun delete(id: String)
}