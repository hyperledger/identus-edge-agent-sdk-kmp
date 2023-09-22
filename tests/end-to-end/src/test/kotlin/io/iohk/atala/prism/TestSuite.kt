package io.iohk.atala.prism

import io.cucumber.java.BeforeAll
import io.cucumber.junit.CucumberOptions
import io.iohk.atala.prism.configuration.Environment
import net.serenitybdd.cucumber.CucumberWithSerenity
import org.junit.runner.RunWith

@RunWith(CucumberWithSerenity::class)
@CucumberOptions(
    features = ["src/test/resources/features"],
    plugin = ["pretty"]
)
class TestSuite

// https://cucumber.io/docs/cucumber/api/?lang=kotlin
@BeforeAll
fun setupEnvironment() {
    Environment.setup()
}
