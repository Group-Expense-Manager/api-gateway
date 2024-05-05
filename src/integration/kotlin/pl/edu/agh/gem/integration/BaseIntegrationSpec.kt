package pl.edu.agh.gem.integration

import io.kotest.core.spec.style.ShouldSpec
import mu.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import pl.edu.agh.gem.AppRunner

@SpringBootTest(
    classes = [AppRunner::class],
    webEnvironment = RANDOM_PORT,
)
@ActiveProfiles("integration")
abstract class BaseIntegrationSpec(body: ShouldSpec.() -> Unit) : ShouldSpec(body) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
