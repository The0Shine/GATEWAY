package com.horob1.gateway.infrastructure.repository

import com.horob1.gateway.domain.user_session.UserSession
import org.springframework.data.repository.CrudRepository

interface UserSessionRedisRepository : CrudRepository<UserSession, String>
