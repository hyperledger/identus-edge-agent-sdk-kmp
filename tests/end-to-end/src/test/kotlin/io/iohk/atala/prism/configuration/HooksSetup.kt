package io.iohk.atala.prism.configuration

import io.cucumber.java.Before
import io.cucumber.java.BeforeAll
import io.cucumber.java.ParameterType
import io.iohk.atala.prism.abilities.UseWalletSdk
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.actors.Cast
import net.serenitybdd.screenplay.actors.OnStage
import net.serenitybdd.screenplay.rest.abilities.CallAnApi

// https://cucumber.io/docs/cucumber/api/?lang=kotlin
@BeforeAll
fun setupEnvironment() {
    Environment.setup()
}

class HooksSetup {
    @Before
    fun setStage() {
        val cast = Cast()

        cast.actorNamed("Edge Agent",
            CallAnApi.at(Environment.mediatorOobUrl),
            UseWalletSdk()
        )

        cast.actorNamed("Cloud Agent",
            CallAnApi.at(Environment.agentUrl)
        )

        OnStage.setTheStage(cast)
    }

    @ParameterType(".*")
    fun actor(actorName: String?): Actor {
        return OnStage.theActorCalled(actorName)
    }
}
