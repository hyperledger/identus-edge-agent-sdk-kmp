package org.hyperledger.identus.walletsdk.workflow

import com.google.gson.GsonBuilder
import io.iohk.atala.automation.serenity.interactions.PollingWait
import io.iohk.atala.automation.utils.Logger
import org.hyperledger.identus.walletsdk.abilities.UseWalletSdk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.rest.interactions.Ensure
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.hamcrest.CoreMatchers.equalTo
import org.hyperledger.identus.walletsdk.abilities.SdkContext
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.IssueCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.OfferCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.OutOfBandInvitation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.RequestPresentation
import org.hyperledger.identus.walletsdk.pluto.PlutoBackupTask
import java.util.function.Consumer

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
                val formattedMessage = requestCredential.makeMessage()
                it.sdk.sendMessage(formattedMessage)
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

    fun processSpecificIssuedCred(edgeAgent: Actor, recordId: String) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute { sdkContext ->
                val issuedCredentialMessage = sdkContext.issuedCredentialStack.removeFirst()
                val issuedCredential = IssueCredential.fromMessage(issuedCredentialMessage)
                val credential = sdkContext.sdk.processIssuedCredentialMessage(issuedCredential)
                edgeAgent.remember(recordId, credential.id)
            }
        )
    }

    fun waitForCredentialRevocationMessage(edgeAgent: Actor, numberOfRevocation: Int) {
        edgeAgent.attemptsTo(
            PollingWait.until(
                UseWalletSdk.revocationStackSize(),
                equalTo(numberOfRevocation)
            )
        )
    }

    fun waitUntilCredentialIsRevoked(edgeAgent: Actor, revokedRecordIdList: List<String>) {
        val revokedIdList = revokedRecordIdList.map { recordId ->
            edgeAgent.recall<String>(recordId)
        }

        edgeAgent.attemptsTo(
            UseWalletSdk.execute { sdkContext ->
                val credentials = sdkContext.sdk.getAllCredentials().first()
                val revokedCredentials = credentials.filter { credential ->
                    credential.revoked == true && revokedIdList.contains(credential.id)
                }
                edgeAgent.attemptsTo(
                    Ensure.that(
                        "The number of revoked credentials matches the expected number",
                        Consumer { context ->
                            assertThat(revokedCredentials.size).isEqualTo(revokedRecordIdList.size)
                        }
                    )
                )
            }
        )
    }

    fun createBackup(edgeAgent: Actor) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute { sdkContext: SdkContext ->
                val backup = sdkContext.sdk.backupWallet(PlutoBackupTask(sdkContext.sdk.pluto))
                val seed = sdkContext.sdk.seed
                edgeAgent.remember("backup", backup)
                edgeAgent.remember("seed", seed)
            }
        )
    }

    fun createANewWalletFromBackup(edgeAgent: Actor) {
        val backup = edgeAgent.recall<String>("backup")
        val seed = edgeAgent.recall<Seed>("seed")
        val walletSdk = UseWalletSdk()
        walletSdk.tearDown() // removes prism.db
        walletSdk.createSdk(seed)
        runBlocking {
            walletSdk.context.sdk.pluto.start()
            walletSdk.context.sdk.recoverWallet(backup)
            walletSdk.context.sdk.start()
            walletSdk.context.sdk.stop()
        }
    }

    fun createNewWalletFromBackupWithWrongSeed(edgeAgent: Actor) {
        val backup = edgeAgent.recall<String>("backup")
        val seed = ApolloImpl().createRandomSeed().seed
        val walletSdk = UseWalletSdk()
        walletSdk.tearDown() // removes prism.db
        walletSdk.createSdk(seed)
        runBlocking {
            walletSdk.context.sdk.pluto.start()
            try {
                walletSdk.context.sdk.recoverWallet(backup)
                fail<String>("SDK should not be able to restore with wrong seed phrase.")
            } catch (e: Exception) {
                assertThat(e).isNotNull()
            }
        }
    }
}
