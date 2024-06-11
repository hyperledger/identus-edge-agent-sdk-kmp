package io.iohk.atala.prism.steps

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.prism.workflow.CloudAgentWorkflow
import io.iohk.atala.prism.workflow.EdgeAgentWorkflow
import net.serenitybdd.screenplay.Actor
import javax.inject.Inject

class CloudAgentSteps {

    @Inject
    private lateinit var cloudAgentWorkflow: CloudAgentWorkflow

    @Inject
    private lateinit var edgeAgentWorkflow: EdgeAgentWorkflow

    @Given("{actor} has a connection invitation with '{}', '{}' and '{}' parameters")
    fun `Cloud Agent has a connection invitation`(cloudAgent: Actor, label: String, goalCode: String, goal: String) {
        val mappedLabel = if (label == "null") { null } else { label }
        val mappedGoalCode = if (goalCode == "null") { null } else { goalCode }
        val mappedGoal = if (goal == "null") { null } else { goal }
        cloudAgentWorkflow.createConnection(cloudAgent, mappedLabel, mappedGoalCode, mappedGoal)
    }

    @Given("{actor} is connected to {actor}")
    fun `Cloud Agent is connected to Edge Agent`(cloudAgent: Actor, edgeAgent: Actor) {
        cloudAgentWorkflow.createConnection(cloudAgent, "alice", "automation", "description")
        cloudAgentWorkflow.shareInvitation(cloudAgent, edgeAgent)
        edgeAgentWorkflow.connect(edgeAgent)
        cloudAgentWorkflow.waitForConnectionState(cloudAgent, "ConnectionResponseSent")
    }

    @Given("{actor} shares invitation to {actor}")
    fun `Cloud Agent shares invitation to Edge Agent`(cloudAgent: Actor, edgeAgent: Actor) {
        cloudAgentWorkflow.shareInvitation(cloudAgent, edgeAgent)
    }

    @When("{actor} offers a credential")
    fun `Cloud Agent offers a credential`(cloudAgent: Actor) {
        cloudAgentWorkflow.offerCredential(cloudAgent)
    }

    @When("{actor} offers an anonymous credential")
    fun `Cloud Agent offers an anonymous credential`(cloudAgent: Actor) {
        cloudAgentWorkflow.offerAnonymousCredential(cloudAgent)
    }

    @When("{actor} should see the credential was accepted")
    fun `Cloud Agent should see the credential was accepted`(cloudAgent: Actor) {
        val recordId = cloudAgent.recall<String>("recordId")
        cloudAgentWorkflow.verifyCredentialState(cloudAgent, recordId, "CredentialSent")
    }

    @When("{actor} asks for present-proof")
    fun `Cloud Agent asks for present-proof`(cloudAgent: Actor) {
        cloudAgentWorkflow.askForPresentProof(cloudAgent)
    }

    @When("{actor} asks for present-proof for anoncred")
    fun `Cloud Agent asks for present-proof for anoncred`(cloudAgent: Actor) {
        cloudAgentWorkflow.askForPresentProofForAnoncred(cloudAgent)
    }

    @When("{actor} revokes '{int}' credentials")
    fun `Cloud Agent revokes {} credentials`(cloudAgent: Actor, numberOfRevokedCredentials: Int) {
        cloudAgentWorkflow.revokeCredential(cloudAgent, numberOfRevokedCredentials)
    }

    @Then("{actor} should have the connection status updated to '{}'")
    fun `Cloud Agent should have the connection status updated`(cloudAgent: Actor, expectedState: String) {
        cloudAgentWorkflow.waitForConnectionState(cloudAgent, expectedState)
    }

    @Then("{actor} should see the present-proof is verified")
    fun `Cloud Agent should see the present-proof is verified`(cloudAgent: Actor) {
        cloudAgentWorkflow.verifyPresentProof(cloudAgent, "PresentationVerified")
    }

    @Then("{actor} should see all credentials were accepted")
    fun `Cloud Agent should see all credentials were accepted`(cloudAgent: Actor) {
        val recordIdList = cloudAgent.recall<List<String>>("recordIdList")
        for (recordId in recordIdList) {
            cloudAgentWorkflow.verifyCredentialState(cloudAgent, recordId, "CredentialSent")
        }
    }
}
