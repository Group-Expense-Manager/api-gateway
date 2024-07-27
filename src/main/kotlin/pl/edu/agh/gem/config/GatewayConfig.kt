package pl.edu.agh.gem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.edu.agh.gem.config.AcceptedHost.ATTACHMENT_STORE
import pl.edu.agh.gem.config.AcceptedHost.AUTHENTICATOR
import pl.edu.agh.gem.config.AcceptedHost.CURRENCY_MANAGER
import pl.edu.agh.gem.config.AcceptedHost.EXPENSE_MANAGER
import pl.edu.agh.gem.config.AcceptedHost.FINANCE_ADAPTER
import pl.edu.agh.gem.config.AcceptedHost.GROUP_MANAGER
import pl.edu.agh.gem.config.AcceptedHost.USER_DETAILS_MANAGER
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.paths.Paths.OPEN

@Configuration
class GatewayConfig(
    private val uriProperties: UriProperties,
    private val retryProperties: RetryProperties,
) {

    @Bean
    fun routes(builder: RouteLocatorBuilder, authFilter: TokenValidationGatewayFilterFactory): RouteLocator {
        return builder.routes()
            .route(authFilter, AUTHENTICATOR, uriProperties.authenticator)
            .route(authFilter, CURRENCY_MANAGER, uriProperties.currencyManager)
            .route(authFilter, GROUP_MANAGER, uriProperties.groupManager)
            .route(authFilter, EXPENSE_MANAGER, uriProperties.expenseManager)
            .route(authFilter, ATTACHMENT_STORE, uriProperties.attachmentStore)
            .route(authFilter, USER_DETAILS_MANAGER, uriProperties.userDetailsManager)
            .route(authFilter, FINANCE_ADAPTER, uriProperties.financeAdapter)
            .build()
    }

    private fun RouteLocatorBuilder.Builder.route(authFilter: TokenValidationGatewayFilterFactory, host: String, routeToUrl: String):
        RouteLocatorBuilder.Builder {
        return route { p: PredicateSpec ->
            p
                .host(host)
                .and()
                .path("$EXTERNAL/**", "$OPEN/**")
                .filters { f ->
                    f
                        .filter(authFilter.apply(TokenValidationGatewayFilterFactory.Config()))
                        .retry { config -> config.setRetries(retryProperties.times).setExceptions(java.io.IOException::class.java) }
                }
                .uri(routeToUrl)
        }
    }
}

@ConfigurationProperties(prefix = "uri")
data class UriProperties(
    val authenticator: String,
    val currencyManager: String,
    val groupManager: String,
    val expenseManager: String,
    val attachmentStore: String,
    val userDetailsManager: String,
    val financeAdapter: String,
)

@ConfigurationProperties(prefix = "retry")
data class RetryProperties(
    val times: Int,
)
