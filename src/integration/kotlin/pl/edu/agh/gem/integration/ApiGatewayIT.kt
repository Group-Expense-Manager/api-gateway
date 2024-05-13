package pl.edu.agh.gem.integration

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.test.web.reactive.server.WebTestClient
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.config.AcceptedHost.AUTHENTICATOR
import pl.edu.agh.gem.integration.ability.stubMicroserviceWithEndpoint
import java.net.URI

@AutoConfigureWebTestClient
class ApiGatewayIT(
    private val webClient: WebTestClient,
) : BaseIntegrationSpec(
    {
        should("not forward request when host is not accepted") {
            // given
            val path = "/open/test"
            val host = "non.existing.host"
            stubMicroserviceWithEndpoint(path)

            // when
            val response = webClient.post()
                .uri(URI(path))
                .header("Host", host)
                .exchange()

            // then
            response shouldHaveHttpStatus NOT_FOUND
        }

        should("not forward request when path is /internal") {
            // given
            val path = "/internal/test"
            stubMicroserviceWithEndpoint(path)

            // when
            val response = webClient.post()
                .uri(URI(path))
                .header("Host", AUTHENTICATOR)
                .exchange()

            // then
            response shouldHaveHttpStatus NOT_FOUND
        }

        should("forward request when endpoint is open and host is accepted") {
            // given
            val url = "/open/test"
            stubMicroserviceWithEndpoint(url)

            // when
            val response = webClient.post()
                .uri(URI(url))
                .header("Host", AUTHENTICATOR)
                .exchange()

            // then
            response shouldHaveHttpStatus OK
        }

        should("not forward request when endpoint is external and host is accepted & no token was provided") {
            // given
            val url = "/external/test"
            stubMicroserviceWithEndpoint(url)

            // when
            val response = webClient.post()
                .uri(URI(url))
                .header("Host", AUTHENTICATOR)
                .exchange()

            // then
            response shouldHaveHttpStatus UNAUTHORIZED
        }

        should("not forward request when endpoint is external and host is accepted & token is not valid") {
            // given
            val url = "/external/test"
            stubMicroserviceWithEndpoint(url)

            // when
            val response = webClient.post()
                .uri(URI(url))
                .header("Host", AUTHENTICATOR)
                .header("Authorization", "Bearer some_token")
                .exchange()

            // then
            response shouldHaveHttpStatus UNAUTHORIZED
        }
    },
)
