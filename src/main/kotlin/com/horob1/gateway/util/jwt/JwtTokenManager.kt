package com.horob1.gateway.util.jwt

import com.horob1.gateway.shared.exception.AppError
import com.horob1.gateway.shared.exception.AppException
import com.horob1.gateway.shared.enums.TokenType
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtTokenManager(
    @param:Value("\${JWT_ACCESS_SECRET}") private val accessSecret: String,
    @param:Value("\${JWT_REFRESH_SECRET}") private val refreshSecret: String,
    @param:Value("\${JWT_EMAIL_SECRET}") private val emailSecret: String,
    @param:Value("\${JWT_RESET_SECRET}") private val resetSecret: String,
    @param:Value("\${JWT_2FA_SECRET}") private val twoFASecret: String,
) {

    private val secretKeys = mapOf(
        TokenType.ACCESS to Keys.hmacShaKeyFor(accessSecret.toByteArray()),
        TokenType.REFRESH to Keys.hmacShaKeyFor(refreshSecret.toByteArray()),
        TokenType.EMAIL_VERIFY to Keys.hmacShaKeyFor(emailSecret.toByteArray()),
        TokenType.PASSWORD_RESET to Keys.hmacShaKeyFor(resetSecret.toByteArray()),
        TokenType.TWO_FA_VERIFY to Keys.hmacShaKeyFor(twoFASecret.toByteArray())
    )

    fun validateAndParse(token: String, tokenType: TokenType): Claims {
        val key = secretKeys[tokenType]
            ?: throw AppException(AppError.InternalServerError)

        try {
            val jwt = Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            return jwt.payload
        } catch (_: ExpiredJwtException) {
            throw AppException(AppError.TokenExpired)
        } catch (_: SignatureException) {
            throw AppException(AppError.InvalidSignature)
        } catch (_: MalformedJwtException) {
            throw AppException(AppError.InvalidToken)
        } catch (_: UnsupportedJwtException) {
            throw AppException(AppError.InvalidToken)
        } catch (_: IllegalArgumentException) {
            throw AppException(AppError.InvalidToken)
        } catch (_: JwtException) {
            throw AppException(AppError.AuthenticationFailed)
        } catch (_: Exception) {
            throw AppException(AppError.InternalServerError)
        }
    }
}
