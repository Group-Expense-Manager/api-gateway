package pl.edu.agh.gem.config

import io.jsonwebtoken.io.IOException
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.edu.agh.gem.config.AcceptedHost.AUTHENTICATOR
import pl.edu.agh.gem.config.AcceptedPath.EXTERNAL
import pl.edu.agh.gem.config.AcceptedPath.OPEN

@Configuration
class GatewayConfig(
    private val uriProperties: UriProperties,
    private val retryProperties: RetryProperties,
) {

    @Bean
    fun routes(builder: RouteLocatorBuilder, authFilter: TokenValidationGatewayFilterFactory): RouteLocator {
        return builder.routes()
            .route { p: PredicateSpec ->
                p
                    .host(AUTHENTICATOR)
                    .and()
                    .path("$EXTERNAL/**", "$OPEN/**")
                    .filters { f ->
                        f
                            .filter(authFilter.apply(TokenValidationGatewayFilterFactory.Config()))
                            .retry { config -> config.setRetries(retryProperties.times).setExceptions(java.io.IOException::class.java) }
                    }
                    .uri(uriProperties.authenticator)
            }
            .build()
    }
}

@ConfigurationProperties(prefix = "uri")
data class UriProperties(
    val authenticator: String,
)

@ConfigurationProperties(prefix = "retry")
data class RetryProperties(
    val times: Int,
)
