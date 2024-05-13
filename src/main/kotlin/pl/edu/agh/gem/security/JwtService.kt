package pl.edu.agh.gem.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
class JwtService(
    private val tokenProperties: TokenProperties,
) {
    fun validateToken(token: String): Jws<Claims> {
        return Jwts.parser()
            .verifyWith(getSecretKey())
            .build()
            .parseSignedClaims(token)
    }

    fun getGemUserFromToken(claims: Jws<Claims>): GemUser {
        val id = claims.payload.subject
        val email = claims.payload["email"] as? String

        return email?.let { GemUser(id, it) } ?: throw JwtException("Email is not valid")
    }

    private fun getSecretKey() = Keys.hmacShaKeyFor(Decoders.BASE64.decode(tokenProperties.secretKey))
}

@ConfigurationProperties(prefix = "token")
data class TokenProperties(
    val secretKey: String,
)
