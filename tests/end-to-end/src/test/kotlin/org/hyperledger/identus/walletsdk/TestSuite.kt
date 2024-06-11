package org.hyperledger.identus.walletsdk

import io.cucumber.junit.CucumberOptions
import net.serenitybdd.cucumber.CucumberWithSerenity
import org.junit.runner.RunWith

@RunWith(CucumberWithSerenity::class)
@CucumberOptions(
    features = ["src/test/resources/features"],
    plugin = ["pretty"],
    tags = "not (@proof and @anoncred)"
)
class TestSuite
