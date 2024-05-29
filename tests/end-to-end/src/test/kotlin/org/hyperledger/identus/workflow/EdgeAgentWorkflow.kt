package org.hyperledger.identus.workflow

import com.google.gson.GsonBuilder
import io.iohk.atala.automation.serenity.interactions.PollingWait
import io.iohk.atala.automation.utils.Logger
import org.hyperledger.identus.abilities.UseWalletSdk
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.IssueCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.OfferCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.RequestPresentation
import kotlinx.coroutines.flow.first
import net.serenitybdd.screenplay.Actor
import org.hamcrest.CoreMatchers.equalTo

class EdgeAgentWorkflow {
    private val logger = Logger.get<EdgeAgentWorkflow>()
    fun connect(edgeAgent: Actor) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute {
                val url = edgeAgent.recall<String>("invitation")
                val oobInvitation = it.sdk.parseInvitation(url)
                try {
                    it.sdk.acceptOutOfBandInvitation(oobInvitation as OutOfBandInvitation)
                } catch (e: CastorError.InvalidDIDString) {
                    logger.error("Error connecting to cloud agent")
                    logger.error("url: $url")
                    val json = GsonBuilder().setPrettyPrinting().create().toJson(oobInvitation)
                    logger.error("oobInvitation: $json")
                    throw e
                }
            }
        )
    }

    fun acceptCredential(edgeAgent: Actor) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute {
                val message = OfferCredential.fromMessage(it.credentialOfferStack.removeFirst())
                val requestCredential = it.sdk.prepareRequestCredentialWithIssuer(it.sdk.createNewPrismDID(), message)
                it.sdk.sendMessage(requestCredential.makeMessage())
            }
        )
    }

    fun waitForProofRequest(edgeAgent: Actor) {
        edgeAgent.attemptsTo(
            PollingWait.until(
                UseWalletSdk.proofOfRequestStackSize(),
                equalTo(1)
            )

        )
    }

    fun presentProof(edgeAgent: Actor) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute {
                val credentials = it.sdk.getAllCredentials().first()
                val credential = credentials.first()
                val requestPresentationMessage = RequestPresentation.fromMessage(it.proofRequestStack.removeFirst())
                val presentation = it.sdk.preparePresentationForRequestProof(requestPresentationMessage, credential)
                it.sdk.sendMessage(presentation.makeMessage())
            }
        )
    }

    fun waitForCredentialOffer(edgeAgent: Actor, numberOfCredentialOffer: Int) {
        edgeAgent.attemptsTo(
            PollingWait.until(
                UseWalletSdk.credentialOfferStackSize(),
                equalTo(numberOfCredentialOffer)
            )
        )
    }

    fun waitToReceiveAnonymousCredential(edgeAgent: Actor, numberOfCredentialOffer: Int) {
        edgeAgent.attemptsTo(
            PollingWait.until(
                UseWalletSdk.credentialOfferStackSize(),
                equalTo(numberOfCredentialOffer)
            )
        )
    }

    fun waitToReceiveCredentialIssuance(edgeAgent: Actor, expectedNumberOfCredentials: Int) {
        edgeAgent.attemptsTo(
            PollingWait.until(
                UseWalletSdk.issuedCredentialStackSize(),
                equalTo(expectedNumberOfCredentials)
            )
        )
    }

    fun processIssuedCredential(edgeAgent: Actor, numberOfCredentials: Int) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute { sdkContext ->
                repeat(numberOfCredentials) {
                    val issuedCredentialMessage = sdkContext.issuedCredentialStack.removeFirst()
                    val issuedCredential = IssueCredential.fromMessage(issuedCredentialMessage)
                    sdkContext.sdk.processIssuedCredentialMessage(issuedCredential)
                }
            }
        )
    }
}
