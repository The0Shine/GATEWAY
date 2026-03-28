package com.horob1.gateway.shared.enums

import java.time.Duration

enum class TokenType(
    val secretEnvKey: String,
    val defaultExpire: Duration
) {
    ACCESS("JWT_ACCESS_SECRET", Duration.ofMinutes(15)),
    REFRESH("JWT_REFRESH_SECRET", Duration.ofDays(100)),
    EMAIL_VERIFY("JWT_EMAIL_SECRET", Duration.ofHours(15)),
    PASSWORD_RESET("JWT_RESET_SECRET", Duration.ofMinutes(15)),
    TWO_FA_VERIFY("JWT_2FA_SECRET", Duration.ofMinutes(15))
}