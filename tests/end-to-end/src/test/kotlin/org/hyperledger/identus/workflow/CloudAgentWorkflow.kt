package org.hyperledger.identus.workflow

import io.iohk.atala.automation.extensions.body
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.matchers.RestAssuredJsonProperty
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.automation.serenity.interactions.PollingWait
import io.iohk.atala.automation.serenity.questions.HttpRequest
import org.hyperledger.identus.configuration.Environment
import io.iohk.atala.prism.models.AnoncredPresentationRequestV1
import io.iohk.atala.prism.models.AnoncredRequestedAttributeV1
import io.iohk.atala.prism.models.AnoncredRequestedPredicateV1
import io.iohk.atala.prism.models.CreateConnectionRequest
import io.iohk.atala.prism.models.CreateIssueCredentialRecordRequest
import io.iohk.atala.prism.models.Options
import io.iohk.atala.prism.models.ProofRequestAux
import io.iohk.atala.prism.models.RequestPresentationInput
import org.hyperledger.identus.utils.Utils
import net.serenitybdd.rest.SerenityRest.lastResponse
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.rest.interactions.Post
import org.apache.http.HttpStatus
import java.util.UUID

class CloudAgentWorkflow {
    fun createConnection(cloudAgent: Actor, label: String?, goalCode: String?, goal: String?) {
        val createConnection = CreateConnectionRequest(
            label,
            goalCode,
            goal
        )

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
            connectionId = UUID.fromString(connectionId),
            schemaId = "${Environment.agentUrl}/schema-registry/schemas/${Environment.jwtSchemaGuid}"
        )
        cloudAgent.attemptsTo(
            Post.to("/issue-credentials/credential-offers").body(credential),
            Ensure.thatTheLastResponse().statusCode().isEqualTo(HttpStatus.SC_CREATED)
        )
        cloudAgent.remember("recordId", lastResponse().get<String>("recordId"))
    }

    fun offerAnonymousCredential(cloudAgent: Actor) {
        val connectionId = cloudAgent.recall<String>("connectionId")
        val credential = CreateIssueCredentialRecordRequest(
            claims = mapOf(
                "name" to "automation",
                "age" to "99",
                "gender" to "M"
            ),
            automaticIssuance = true,
            issuingDID = Environment.publishedDid,
            connectionId = UUID.fromString(connectionId),
            credentialFormat = "AnonCreds",
            credentialDefinitionId = UUID.fromString(Environment.anoncredDefinitionId)
        )

        cloudAgent.attemptsTo(
            Post.to("/issue-credentials/credential-offers").body(credential),
            Ensure.thatTheLastResponse().statusCode().isEqualTo(HttpStatus.SC_CREATED)
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
            connectionId = UUID.fromString(connectionId),
            options = options,
            proofs = listOf(proofs)
        )

        cloudAgent.attemptsTo(
            Post.to("/present-proof/presentations").body(presentProofRequest),
            Ensure.thatTheLastResponse().statusCode().isEqualTo(HttpStatus.SC_CREATED)
        )
        cloudAgent.remember("presentationId", lastResponse().get<String>("presentationId"))
    }

    fun askForPresentProofForAnoncred(cloudAgent: Actor) {
        val credentialDefinitionId = Environment.agentUrl +
                "/credential-definition-registry/definitions/" +
                Environment.anoncredDefinitionId +
                "/definition"
        val anoncredsPresentationRequestV1 = AnoncredPresentationRequestV1(
            requestedAttributes = mapOf(
                "gender" to AnoncredRequestedAttributeV1(
                    name = "gender",
                    restrictions = listOf(
                        mapOf(
                            "attr::gender::value" to "M",
                            "cred_def_id" to credentialDefinitionId
                        )
                    )
                )
            ),
            requestedPredicates = mapOf(
                "age" to AnoncredRequestedPredicateV1(
                    name = "age",
                    pType = ">",
                    pValue = 18,
                    restrictions = emptyList()
                )
            ),
            name = "proof_req_1",
            nonce = Utils.generateNonce(25),
            version = "0.1"
        )

        val presentProofRequest = RequestPresentationInput(
            connectionId = UUID.fromString(cloudAgent.recall("connectionId")),
            credentialFormat = "AnonCreds",
            anoncredPresentationRequest = anoncredsPresentationRequestV1,
            proofs = emptyList()
        )

        cloudAgent.attemptsTo(
            Post.to("/present-proof/presentations").body(presentProofRequest),
            Ensure.thatTheLastResponse().statusCode().isEqualTo(HttpStatus.SC_CREATED)
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
