package io.iohk.atala.prism.steps

import io.cucumber.java.After
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.prism.abilities.UseWalletSdk
import io.iohk.atala.prism.workflow.CloudAgentWorkflow
import io.iohk.atala.prism.workflow.EdgeAgentWorkflow
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.actors.OnStage
import javax.inject.Inject

class EdgeAgentSteps {

    @Inject
    private lateinit var edgeAgentWorkflow: EdgeAgentWorkflow

    @Inject
    private lateinit var cloudAgentWorkflow: CloudAgentWorkflow

    @When("{actor} connects through the invite")
    fun `Edge Agent connects through the invite`(edgeAgent: Actor) {
        edgeAgentWorkflow.connect(edgeAgent)
    }

    @When("{actor} has an issued credential from {actor}")
    fun `Edge Agent has an issued credential`(edgeAgent: Actor, cloudAgent: Actor) {
        cloudAgentWorkflow.offerCredential(cloudAgent)
        edgeAgentWorkflow.waitToReceiveCredentialIssuance(edgeAgent, 1)
        edgeAgentWorkflow.acceptCredential(edgeAgent)
    }

    @When("{actor} accepts {} credential offer sequentially from {actor}")
    fun `Edge Agent accepts multiple credentials offer sequentially from Cloud Agent`(
        edgeAgent: Actor,
        numberOfCredentials: Int,
        cloudAgent: Actor
    ) {
        val recordIdList = mutableListOf<String>()
        repeat(numberOfCredentials) {
            cloudAgentWorkflow.offerCredential(cloudAgent)
            edgeAgentWorkflow.waitForCredentialOffer(edgeAgent, 1)
            edgeAgentWorkflow.acceptCredential(edgeAgent)
            cloudAgentWorkflow.verifyCredentialState(cloudAgent, cloudAgent.recall("recordId"), "CredentialSent")
            recordIdList.add(cloudAgent.recall("recordId"))
        }
        cloudAgent.remember("recordIdList", recordIdList)
    }

    @When("{actor} accepts {} credentials offer at once from {actor}")
    fun `Edge Agent accepts multiple credentials offer at once from Cloud Agent`(
        edgeAgent: Actor,
        numberOfCredentials: Int,
        cloudAgent: Actor
    ) {
        val recordIdList = mutableListOf<String>()

        // offer multiple credentials
        repeat(numberOfCredentials) {
            cloudAgentWorkflow.offerCredential(cloudAgent)
            recordIdList.add(cloudAgent.recall("recordId"))
        }

        // wait to receive
        edgeAgentWorkflow.waitForCredentialOffer(edgeAgent, 3)

        // accept all
        repeat(numberOfCredentials) {
            edgeAgentWorkflow.acceptCredential(edgeAgent)
        }
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
        edgeAgentWorkflow.waitForCredentialOffer(edgeAgent, 1)
    }

    @Then("{actor} wait to receive {} issued credentials")
    fun `Edge Agent wait to receive issued credentials`(edgeAgent: Actor, expectedNumberOfCredentials: Int) {
        edgeAgentWorkflow.waitToReceiveCredentialIssuance(edgeAgent, expectedNumberOfCredentials)
    }

    @Then("{actor} process {} issued credentials")
    fun `Edge Agent process multiple issued credentials`(edgeAgent: Actor, numberOfCredentials: Int) {
        edgeAgentWorkflow.processIssuedCredential(edgeAgent, numberOfCredentials)
    }

    @After
    fun stopAgent() {
        OnStage.theActor("Edge Agent").attemptsTo(
            UseWalletSdk.stop()
        )
    }
}