package pl.edu.agh.gem.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.jsonwebtoken.JwtException
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import pl.edu.agh.gem.headers.CustomHeaders.X_OAUTH_TOKEN_VALIDATED
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.security.GemUser
import pl.edu.agh.gem.security.JwtService
import reactor.core.publisher.Mono

@Component
class TokenValidationGatewayFilterFactory(
    private val jwtService: JwtService,
) : AbstractGatewayFilterFactory<TokenValidationGatewayFilterFactory.Config>() {
    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val path = request.uri.path
            if (path.startsWith(EXTERNAL)) {
                try {
                    val authorization = extractAuthorization(request) ?: throw AuthorizationException()
                    val user = extractGemUser(authorization)
                    val modifiedRequest = addGemUserToHeader(exchange, user)
                    chain.filter(exchange.mutate().request(modifiedRequest).build())
                } catch (ex: Exception) {
                    when (ex) {
                        is JwtException, is AuthorizationException -> setRequestAsUnauthorized(exchange)
                        else -> throw ex
                    }
                }
            } else {
                chain.filter(exchange)
            }
        }
    }

    private fun addGemUserToHeader(
        exchange: ServerWebExchange,
        user: GemUser,
    ) = exchange.request
        .mutate()
        .header(X_OAUTH_TOKEN_VALIDATED, jacksonObjectMapper().writeValueAsString(user))
        .build()

    private fun extractGemUser(authorization: String): GemUser {
        val token = authorization.substringAfter(" ")
        val claims = jwtService.validateToken(token)
        return jwtService.getGemUserFromToken(claims)
    }

    private fun setRequestAsUnauthorized(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = UNAUTHORIZED
        return exchange.response.setComplete()
    }

    private fun extractAuthorization(request: ServerHttpRequest) = request.headers.getFirst("Authorization")

    class AuthorizationException : RuntimeException("Authorization header not present")

    class Config
}
