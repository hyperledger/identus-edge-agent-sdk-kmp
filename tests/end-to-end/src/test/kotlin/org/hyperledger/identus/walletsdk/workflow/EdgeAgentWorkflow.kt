package org.hyperledger.identus.walletsdk.workflow

import com.google.gson.GsonBuilder
import io.iohk.atala.automation.serenity.interactions.PollingWait
import io.iohk.atala.automation.utils.Logger
import io.ktor.util.reflect.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.rest.abilities.CallAnApi
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.hamcrest.CoreMatchers.equalTo
import org.hyperledger.identus.walletsdk.abilities.SdkContext
import org.hyperledger.identus.walletsdk.abilities.UseWalletSdk
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.configuration.Environment
import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.PresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.ProvableCredential
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.IssueCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.OfferCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.OutOfBandInvitation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.RequestPresentation
import org.hyperledger.identus.walletsdk.pluto.PlutoBackupTask
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

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
                assertThat(credential).instanceOf(ProvableCredential::class)
                if (credential is ProvableCredential) {
                    val requestPresentationMessage = RequestPresentation.fromMessage(it.proofRequestStack.removeFirst())
                    val presentation = it.sdk.preparePresentationForRequestProof(requestPresentationMessage, credential)
                    it.sdk.sendMessage(presentation.makeMessage())
                }
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
            PollingWait.with(2.minutes, 500.milliseconds).until(
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
                assertThat(revokedCredentials.size).isEqualTo(revokedRecordIdList.size)
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
        walletSdk.recoverWallet(seed, backup)
        runBlocking {
            walletSdk.tearDown()
        }
    }

    fun createNewWalletFromBackupWithWrongSeed(edgeAgent: Actor) {
        val backup = edgeAgent.recall<String>("backup")
        val seed = ApolloImpl().createRandomSeed().seed
        val walletSdk = UseWalletSdk()
        runBlocking {
            try {
                walletSdk.recoverWallet(seed, backup)
                fail<String>("SDK should not be able to restore with wrong seed phrase.")
            } catch (e: Exception) {
                assertThat(e).isNotNull()
            }
        }
    }

    fun createPeerDids(edgeAgent: Actor, numberOfDids: Int) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute { sdkContext ->
                repeat(numberOfDids) {
                    val did = sdkContext.sdk.createNewPeerDID(updateMediator = true)
                    edgeAgent.remember("did", did)
                }
            }
        )
    }

    fun createPrismDids(edgeAgent: Actor, numberOfDids: Int) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute { sdkContext ->
                repeat(numberOfDids) {
                    sdkContext.sdk.createNewPrismDID()
                }
            }
        )
    }

    fun backupAndRestoreToNewAgent(newAgent: Actor, originalAgent: Actor) {
        val backup = originalAgent.recall<String>("backup")
        val seed = originalAgent.recall<Seed>("seed")
        val walletSdk = UseWalletSdk()
        walletSdk.recoverWallet(seed, backup)
        newAgent.whoCan(walletSdk).whoCan(CallAnApi.at(Environment.mediatorOobUrl))
    }

    fun copyAgentShouldMatchOriginalAgent(restoredEdgeAgent: Actor, originalEdgeAgent: Actor) {
        val expectedCredentials = mutableListOf<String>()
        val expectedPeerDids = mutableListOf<String>()
        val expectedPrismDids = mutableListOf<String>()

        originalEdgeAgent.attemptsTo(
            UseWalletSdk.execute { sdkContext ->
                expectedCredentials.addAll(sdkContext.sdk.getAllCredentials().first().map { it.id })
                expectedPeerDids.addAll(sdkContext.sdk.pluto.getAllPeerDIDs().first().map { it.did.toString() })
                expectedPrismDids.addAll(sdkContext.sdk.pluto.getAllPrismDIDs().first().map { it.did.toString() })
            }
        )

        restoredEdgeAgent.attemptsTo(
            UseWalletSdk.execute { sdkContext ->
                val actualCredentials = sdkContext.sdk.getAllCredentials().first().map { it.id }
                val actualPeerDids = sdkContext.sdk.pluto.getAllPeerDIDs().first().map { it.did.toString() }
                val actualPrismDids = sdkContext.sdk.pluto.getAllPrismDIDs().first().map { it.did.toString() }

                assertThat(actualCredentials.size).isEqualTo(expectedCredentials.size)
                assertThat(actualCredentials.containsAll(expectedCredentials)).isTrue()
                assertThat(actualPeerDids.size).isEqualTo(expectedPeerDids.size)
                assertThat(actualPeerDids.containsAll(expectedPeerDids)).isTrue()
                assertThat(actualPrismDids.size).isEqualTo(expectedPrismDids.size)
                assertThat(actualPrismDids.containsAll(expectedPrismDids)).isTrue()
            }
        )
    }

    fun initiatePresentationRequest(type: CredentialType, edgeAgent: Actor, did: DID, claims: PresentationClaims) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute {
                it.sdk.initiatePresentationRequest(type, did, claims, "", UUID.randomUUID().toString())
            }
        )
    }

    fun waitForPresentationMessage(edgeAgent: Actor, numberOfMessages: Int = 1) {
        edgeAgent.attemptsTo(
            PollingWait.until(
                UseWalletSdk.presentationStackSize(),
                equalTo(numberOfMessages)
            )
        )
    }

    fun verifyPresentation(edgeAgent: Actor, expected: Boolean = true, shouldBeRevoked: Boolean = false) {
        edgeAgent.attemptsTo(
            UseWalletSdk.execute {
                val message = it.presentationStack.removeFirst()
                try {
                    val isVerified = it.sdk.handlePresentation(message)
                    assertThat(isVerified).isEqualTo(expected)
                } catch (e: PolluxError.VerificationUnsuccessful) {
                    assertThat(expected).isFalse()
                    assertThat(shouldBeRevoked).isTrue()
                    assertThat(e.message).contains("Provided credential is revoked")
                }
            }
        )
    }
}
