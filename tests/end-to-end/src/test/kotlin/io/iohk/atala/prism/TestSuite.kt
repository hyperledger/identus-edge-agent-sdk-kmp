package io.iohk.atala.prism

import io.cucumber.junit.CucumberOptions
import io.iohk.atala.prism.configuration.Environment
import net.serenitybdd.cucumber.CucumberWithSerenity
import org.junit.BeforeClass
import org.junit.runner.RunWith

@RunWith(CucumberWithSerenity::class)
@CucumberOptions(
    features = ["src/test/resources/features"],
    plugin = ["pretty"]
)
class TestSuite {
    companion object {
        @JvmStatic
        @BeforeClass
        fun setupEnvironment() {
            Environment.setup()
        }
    }
}

