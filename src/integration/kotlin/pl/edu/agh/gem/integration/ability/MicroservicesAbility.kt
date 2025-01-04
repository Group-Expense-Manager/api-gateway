package pl.edu.agh.gem.integration.ability

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.environment.ProjectConfig

fun stubMicroserviceWithEndpoint(
    url: String,
    statusCode: HttpStatusCode = HttpStatus.OK,
) {
    ProjectConfig.wiremock.stubFor(
        post(urlMatching(url))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType(),
            ),
    )
}
