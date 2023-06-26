package io.iohk.atala.prism.steps

import io.cucumber.java.After
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.prism.abilities.UseWalletSdk
import io.iohk.atala.prism.workflow.EdgeAgentWorkflow
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.actors.OnStage
import javax.inject.Inject

class EdgeAgentSteps {

    @Inject
    private lateinit var edgeAgentWorkflow: EdgeAgentWorkflow

    @When("{actor} connects through the invite")
    fun `Edge Agent connects through the invite`(edgeAgent: Actor) {
        edgeAgentWorkflow.connect(edgeAgent)
    }

    @When("{actor} accepts the credential")
    fun `Edge Agent accepts the credential`(edgeAgent: Actor) {
        edgeAgentWorkflow.acceptCredential(edgeAgent)
    }

    @When("{actor} sends the present-proof")
    fun `Edge Agent sends the present-proof`(edgeAgent: Actor) {
        edgeAgentWorkflow.waitForProofRequest(edgeAgent)
        edgeAgentWorkflow.presentProof(edgeAgent)
    }

    @Then("{actor} should receive the credential")
    fun `Edge Agent should receive the credential`(edgeAgent: Actor) {
        edgeAgentWorkflow.waitForCredentialOffer(edgeAgent)
    }

    @Then("{actor} wait to receive an issued credential")
    fun `Edge Agent wait to receive an issued credential`(edgeAgent: Actor) {
        edgeAgentWorkflow.waitToReceiveIssuedCredential(edgeAgent)
    }

    @Then("{actor} process the issued credential")
    fun `Edge Agent process the issued credential`(edgeAgent: Actor) {
        edgeAgentWorkflow.processIssuedCredential(edgeAgent)
    }

    @After
    fun stopAgent() {
        OnStage.theActor("Edge Agent").attemptsTo(
            UseWalletSdk.stop()
        )
    }
}