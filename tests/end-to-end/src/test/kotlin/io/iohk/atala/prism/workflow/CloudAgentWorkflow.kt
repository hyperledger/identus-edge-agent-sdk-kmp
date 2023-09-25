package io.iohk.atala.prism.workflow

import io.iohk.atala.automation.extensions.body
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.matchers.RestAssuredJsonProperty
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.automation.serenity.interactions.PollingWait
import io.iohk.atala.automation.serenity.questions.HttpRequest
import io.iohk.atala.prism.configuration.Environment
import io.iohk.atala.prism.models.*
import net.serenitybdd.rest.SerenityRest.lastResponse
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.rest.interactions.Post
import org.apache.http.HttpStatus
import java.util.*

class CloudAgentWorkflow {
    fun createConnection(cloudAgent: Actor) {
        val createConnection = CreateConnectionRequest("Alice")
        cloudAgent.attemptsTo(
            Post.to("/connections").body(createConnection),
            Ensure.thatTheLastResponse().statusCode().isEqualTo(HttpStatus.SC_CREATED)
        )

        cloudAgent.remember("invitation", lastResponse().get<String>("invitation.invitationUrl"))
        cloudAgent.remember("connectionId", lastResponse().get<String>("connectionId"))
    }

    fun shareInvitation(cloudAgent: Actor, edgeAgent: Actor) {
        val invitation = cloudAgent.recall<String>("invitation")
        edgeAgent.remember("invitation", invitation)
    }

    fun waitForConnectionState(cloudAgent: Actor, state: String) {
        val connectionId = cloudAgent.recall<String>("connectionId")
        cloudAgent.attemptsTo(
            PollingWait.until(
                HttpRequest.get("/connections/$connectionId"),
                RestAssuredJsonProperty.toBe("state", state)
            )
        )
    }

    fun offerCredential(cloudAgent: Actor) {
        val connectionId = cloudAgent.recall<String>("connectionId")
        val credential = CreateIssueCredentialRecordRequest(
            claims = mapOf(Pair("automation-required", UUID.randomUUID())),
            issuingDID = Environment.publishedDid,
            connectionId = connectionId,
            schemaId = "${Environment.agentUrl}/schema-registry/schemas/${Environment.schemaId}"
        )
        cloudAgent.attemptsTo(
            Post.to("/issue-credentials/credential-offers").body(credential)
        )
        cloudAgent.remember("recordId", lastResponse().get<String>("recordId"))
    }

    fun askForPresentProof(cloudAgent: Actor) {
        val connectionId = cloudAgent.recall<String>("connectionId")

        val options = Options(
            challenge = UUID.randomUUID().toString(),
            domain = Environment.agentUrl
        )

        val proofs = ProofRequestAux(
            schemaId = "https://schema.org/Person",
            trustIssuers = listOf("") // TODO: use publicated DID
        )
        val presentProofRequest = RequestPresentationInput(
            connectionId = connectionId,
            options = options,
            proofs = listOf(proofs)
        )

        cloudAgent.attemptsTo(
            Post.to("/present-proof/presentations").body(presentProofRequest)
        )
        cloudAgent.remember("presentationId", lastResponse().get<String>("presentationId"))
    }

    fun verifyCredentialState(cloudAgent: Actor, recordId: String, state: String) {
        cloudAgent.attemptsTo(
            PollingWait.until(
                HttpRequest.get("/issue-credentials/records/$recordId"),
                RestAssuredJsonProperty.toBe("protocolState", state)
            )
        )
    }

    fun verifyPresentProof(cloudAgent: Actor, state: String) {
        val presentationId = cloudAgent.recall<String>("presentationId")
        cloudAgent.attemptsTo(
            PollingWait.until(
                HttpRequest.get("/present-proof/presentations/$presentationId"),
                RestAssuredJsonProperty.toBe("status", state)
            )
        )
    }
}
