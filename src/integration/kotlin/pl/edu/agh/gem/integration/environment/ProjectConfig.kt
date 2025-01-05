package pl.edu.agh.gem.integration.environment

import com.github.tomakehurst.wiremock.WireMockServer
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

object ProjectConfig : AbstractProjectConfig() {
    private const val WIREMOCK_SERVER_PORT = 9999

    val wiremock = WireMockServer(WIREMOCK_SERVER_PORT)
    private val wiremockListener = WireMockListener(wiremock)

    override fun extensions() =
        listOf(
            wiremockListener,
            SpringExtension,
        )
}
