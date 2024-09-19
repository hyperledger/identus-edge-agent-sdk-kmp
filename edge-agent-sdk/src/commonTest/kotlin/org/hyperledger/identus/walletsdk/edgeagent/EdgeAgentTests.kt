@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.edgeagent

import anoncreds_uniffi.createLinkSecret
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEObject
import io.ktor.http.HttpStatusCode
import java.security.interfaces.ECPublicKey
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.hyperledger.identus.apollo.base64.base64UrlDecoded
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.apollo.derivation.MnemonicHelper
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.X25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PrivateKey
import org.hyperledger.identus.walletsdk.castor.CastorImpl
import org.hyperledger.identus.walletsdk.domain.DIDCOMM_MESSAGING
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Mercury
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pollux
import org.hyperledger.identus.walletsdk.domain.models.AnoncredsInputFieldFilter
import org.hyperledger.identus.walletsdk.domain.models.AnoncredsPresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.ApolloError
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentBase64
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.ClaimType
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDPair
import org.hyperledger.identus.walletsdk.domain.models.DIDResolver
import org.hyperledger.identus.walletsdk.domain.models.DIDUrl
import org.hyperledger.identus.walletsdk.domain.models.HttpResponse
import org.hyperledger.identus.walletsdk.domain.models.JWTPresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.KeyCurve
import org.hyperledger.identus.walletsdk.domain.models.Mediator
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.ProvableCredential
import org.hyperledger.identus.walletsdk.domain.models.RequestedAttributes
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.Signature
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.DerivationPathKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SeedKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorablePrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.TypeKey
import org.hyperledger.identus.walletsdk.edgeagent.helpers.AgentOptions
import org.hyperledger.identus.walletsdk.edgeagent.mediation.MediationHandler
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.IssueCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.OfferCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.ConnectionlessCredentialOffer
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.ConnectionlessRequestPresentation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.OutOfBandInvitation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.PrismOnboardingInvitation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.RequestPresentation
import org.hyperledger.identus.walletsdk.logger.LoggerMock
import org.hyperledger.identus.walletsdk.mercury.ApiMock
import org.hyperledger.identus.walletsdk.pluto.CredentialRecovery
import org.hyperledger.identus.walletsdk.pluto.PlutoBackupTask
import org.hyperledger.identus.walletsdk.pluto.PlutoRestoreTask
import org.hyperledger.identus.walletsdk.pluto.RestorationID
import org.hyperledger.identus.walletsdk.pluto.models.backup.BackupV0_0_1
import org.hyperledger.identus.walletsdk.pollux.PolluxImpl
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.CredentialRequestMeta
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.hyperledger.identus.walletsdk.pollux.models.PresentationSubmission
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EdgeAgentTests {

    @Mock
    lateinit var apolloMock: Apollo

    @Mock
    lateinit var plutoMock: Pluto

    @Mock
    lateinit var mercuryMock: Mercury

    @Mock
    lateinit var polluxMock: Pollux

    @Mock
    lateinit var castorMock: Castor

    @Mock
    lateinit var connectionManagerMock: ConnectionManager

    @Mock
    lateinit var mediationHandlerMock: MediationHandler

    lateinit var apolloMockOld: ApolloMock
    lateinit var castorMockOld: CastorMock
    lateinit var plutoMockOld: PlutoMock
    lateinit var mercuryMockOld: MercuryMock
    lateinit var polluxMockOld: PolluxMock
    lateinit var mediationHandlerMockOld: MediationHandlerMock
    lateinit var connectionManagerOld: ConnectionManager
    lateinit var json: Json
    lateinit var seed: Seed

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        seed = Seed(MnemonicHelper.createRandomSeed())
        apolloMockOld = ApolloMock()
        castorMockOld = CastorMock()
        plutoMockOld = PlutoMock()
        mercuryMockOld = MercuryMock()
        polluxMockOld = PolluxMock()
        mediationHandlerMockOld = MediationHandlerMock()
        // Pairing will be removed in the future
        connectionManagerOld =
            ConnectionManagerImpl(
                mercuryMockOld,
                castorMockOld,
                plutoMockOld,
                mediationHandlerMockOld,
                mutableListOf(),
                polluxMockOld
            )
        json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }

    @Test
    fun `EdgeAgent start should create peer did and register mediator if no mediator available`() = runTest {
        val connectionManager = ConnectionManagerImpl(
            mercuryMockOld,
            castorMockOld,
            plutoMock,
            mediationHandlerMock,
            mutableListOf(),
            polluxMock
        )

        val agent = spy(
            EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManager,
                seed = null,
                api = null,
                logger = LoggerMock(),
                agentOptions = AgentOptions()
            )
        )

        val mediator = Mediator(
            mediatorDID = DID("did:peer:asdf"),
            hostDID = DID("did:peer:asdf"),
            routingDID = DID("did:peer:asdf")
        )
        `when`(mediationHandlerMock.bootRegisteredMediator()).thenReturn(null)
        `when`(mediationHandlerMock.achieveMediation(any())).thenReturn(flow { emit(mediator) })
        `when`(mediationHandlerMock.mediator).thenReturn(mediator)
        `when`(mediationHandlerMock.mediatorDID).thenReturn(mediator.mediatorDID)

        `when`(agent.createNewPeerDID(updateMediator = false)).thenReturn(DID("did:peer:asdf"))
        agent.start()

        assertEquals(EdgeAgent.State.RUNNING, agent.state)
    }

    @Test
    fun `EdgeAgent start should throw MediationRequestFailedError if mediatiorhandler mediator is null`() = runTest {
        val connectionManager = ConnectionManagerImpl(
            mercuryMockOld,
            castorMockOld,
            plutoMock,
            mediationHandlerMock,
            mutableListOf(),
            polluxMock
        )

        val agent = spy(
            EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManager,
                seed = null,
                api = null,
                logger = LoggerMock(),
                agentOptions = AgentOptions()
            )
        )

        val mediator = Mediator(
            mediatorDID = DID("did:peer:asdf"),
            hostDID = DID("did:peer:asdf"),
            routingDID = DID("did:peer:asdf")
        )
        `when`(mediationHandlerMock.bootRegisteredMediator()).thenReturn(null)
        `when`(mediationHandlerMock.achieveMediation(any())).thenReturn(flow { emit(mediator) })
        `when`(mediationHandlerMock.mediator).thenReturn(null)

        `when`(agent.createNewPeerDID(updateMediator = false)).thenReturn(DID("did:peer:asdf"))

        assertFailsWith(EdgeAgentError.MediationRequestFailedError::class) {
            agent.start()
            assertEquals(EdgeAgent.State.STOPPED, agent.state)
        }
    }

    @Test
    fun `EdgeAgent stop should change the state to stop`() = runTest {
        val agent = spy(
            EdgeAgent(
                apollo = apolloMock,
                castor = castorMock,
                pluto = plutoMock,
                mercury = mercuryMockOld,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = null,
                logger = LoggerMock(),
                agentOptions = AgentOptions()
            )
        )
        agent.stop()
        assertEquals(EdgeAgent.State.STOPPED, agent.state)
    }

    @Test
    fun `EdgeAgent setupMediatorHandler should stop the agent and replace the current mediatior handler`() = runTest {
        val agent = spy(
            EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManagerMock,
                seed = null,
                api = null,
                logger = LoggerMock(),
                agentOptions = AgentOptions()
            )
        )
        val mediatorHandlerMock2 = mock<MediationHandler>()
        agent.setupMediatorHandler(mediatorHandlerMock2)
        assertEquals(EdgeAgent.State.STOPPED, agent.state)
        assertEquals(agent.connectionManager.mediationHandler, mediatorHandlerMock2)
    }

    @Test
    fun `EdgeAgent setupMediatorDID create a new mediator handler and call setup mediator handler`() = runTest {
        val agent = spy(
            EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManagerMock,
                seed = null,
                api = null,
                logger = LoggerMock(),
                agentOptions = AgentOptions()
            )
        )

        val mediatorHandler = agent.connectionManager.mediationHandler
        agent.setupMediatorDID(DID("did:peer:asdf"))
        assertEquals(EdgeAgent.State.STOPPED, agent.state)
        assertNotEquals(agent.connectionManager.mediationHandler, mediatorHandler)
    }

    @Test
    fun `EdgeAgent send message should call connection manager send message`() = runTest {
        val agent = spy(
            EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManagerMock,
                seed = null,
                api = null,
                logger = LoggerMock(),
                agentOptions = AgentOptions()
            )
        )

        val message = Json.decodeFromString<Message>(
            """{"id":"e430e4af-455e-4a15-9f2f-5bd8e5f350b8","piuri":"https://didcomm.org/issue-credential/3.0/offer-credential","from":{"method":"peer","methodId":"2.Ez6LSm5hETc4CS4X8RxYYKjoS2B3CM8TyzbgRrE7kGrdymHdq.Vz6MkoP2VXs4N7iNsKTzEKtZbnfu6yDH1x2ajGtCmNmc6qdMW.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},"to":{"method":"peer","methodId":"2.Ez6LSd8irQSWjjMfvg53kcaDY6Q2doPEvQwscjSzidgWoFUVK.Vz6Mksu4QVe8oKwJEDPgxRg2bFa3QWrZR1EZGC9xq8xk9twYX.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},"fromPrior":"null","body":"{\"multiple_available\":null,\"goal_code\":\"Offer Credential\",\"credential_preview\":{\"schema_id\":\"http:\\/\\/192.168.68.113:8000\\/cloud-agent\\/schema-registry\\/schemas\\/5667190d-640c-36af-a9f1-f4ed2587e766\\/schema\",\"type\":\"https:\\/\\/didcomm.org\\/issue-credential\\/3.0\\/credential-credential\",\"body\":{\"attributes\":[{\"media_type\":null,\"name\":\"age\",\"value\":\"18\"},{\"media_type\":null,\"name\":\"name\",\"value\":\"Cristian\"}]}},\"replacement_id\":null,\"comment\":null}","created_time":"1721242264","expires_time_plus":"1721328667","attachments":[{"id":"ee903fe0-2c49-4356-9b41-cfccc979c0a1","data":{"base64":"eyJzY2hlbWFfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9jbG91ZC1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy81NjY3MTkwZC02NDBjLTM2YWYtYTlmMS1mNGVkMjU4N2U3NjYvc2NoZW1hIiwiY3JlZF9kZWZfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9jbG91ZC1hZ2VudC9jcmVkZW50aWFsLWRlZmluaXRpb24tcmVnaXN0cnkvZGVmaW5pdGlvbnMvM2RhNmExZDgtMmIyMy0zMTM4LWIwMmEtYWIwYmI0OGY4MGY5L2RlZmluaXRpb24iLCJrZXlfY29ycmVjdG5lc3NfcHJvb2YiOnsiYyI6IjM0MjE0MzM4OTA5MDk4MTU5ODIyNTA3MjQ4Njk3NjUyNTIyMzc1NzM2ODM0OTM1MDg0NjM5MTYzNTUyNjgwMjc3MjQyOTcxODI5ODUwIiwieHpfY2FwIjoiODAwNzA3NDcxNzUyOTQ3MTI1NDIxNTU2ODI1ODYyNDc5NzE1OTE0OTE3ODE4NTY4MDI1NjU5Mzk1NDcyMTAzMzQ0NjAwMDI4NzU4MzczMTA5Mjg0NzEzNDg5MDg1OTk3NzE5NDcwOTc5MDQ1NzA3NTY1ODA4MDczNzYxMjI0OTI3MzcyMDk1MTU2MzAxODE3MzgzNDE5NzE4NzM0NDk2MDQwMjc1MzM2MTg0ODIzNzQ4NTg0NzgzNjIxNzE1NTQwOTI3Nzk0ODQyMTA2NDM1NDEwNzc4MDg2ODg1MTc5MzQzMjEzMjU3NTk2ODM2NjU5NzYwMTI4NzI5ODI5MTk2MzI0MjQwMzgyOTc3MzczNjU3MTA0NjQ5NjE4MjU0MDMzMDk4Njc0OTkxMzIwMzc2NTEyMTUyOTk3Mzg0Mzk5MzY0OTc3MDM4NzU2ODcwOTU3NTcyMDM0NTM0NTY1MDM5OTY0MTYzNDgzNDEyMTEzNzc0NzA5MTU2NTcxMjcxNTI3ODY2NzQwNzU1Nzc2MzIzOTgxNTE5NzEzNjQ1MjQ4Njc5NzgyOTM3NTcyOTI4NDI1NzQ0NjE3MjMwNzk2MzYwNzE5MDA3Mzc0ODgwNTI2ODA4ODIzODg2NTMwMzE2MzgyOTI1MDcxOTYzMjUwMzgyNzU4MDA5Mjk1MzI1NDgzODIyNTg0OTIyMDkwMTcwMTAxOTY5OTk5MTg1MzAxMDA1NDM2MjYyMDI4NDIxOTA0NjU5MzY4OTU3OTQyNTAwNDkxNzI5MjY0MTMzMzE5Nzg0NDIwNjQ0NDUxMTUyNjA2MDI2MDg1MDkwNDYzNzAzMTQzNDczMTcxMjg3OTUxMTM0NTM1MDY0NDc0NjQ5NDE5MzUzMDQ4MzQwOTk4MTY2NjYwNDkyMDE5ODQyNDU2NDM5MTcxNjM2IiwieHJfY2FwIjpbWyJhZ2UiLCI3MjA2NjI4MDAzNzAwMjM5MzM5NjAzNjUxNDQ1MTY2NDE0MjcwNTA0ODM4ODAwMzYxMjE0NjQ3MjA1ODYwOTczMTg4MTQ0OTE2OTkwOTE3NTIyNjU4Mzg3Njg1MTcwMzkyMDcyMjk0MTUxMDUwODk1Njc0MTU0OTYwMDMyMDM0Mzg2MzEzMzY0MTMwMDQwNzI5ODQwMjcxMzcyNjc4NTI0NjE0NjUxNDcxNDU0NTg4MzgwMzI2OTMwODQ0Njc4Nzg3MzA3NzY3ODk2NjY5NTE2MDY1Njc3MDA3MjQyMDEwMjQyMDAwNTg4NjgxMTczNDUxNjg4NTU5MDEwMjQyNTgyMTg4MTY5MTUyNjUwNzY4NzgxNjMyMjgwNTgyODI1NjM3MjY0NzUwMTA3NTU2NDQzNTgyNzMwNDIxMjE3NTI4OTgyNTE5MzA3NzQ0ODAxNTYyNTYyMzQzNTcyNzU4NDEzNjc1NzY0ODQwOTY5MTY3NTE3ODcyNjk2MDY1MDM2MDU1MzgwMDg2NjcyNjUzMDEyMTIxMDk2MTA5OTQyMTg1NjM2ODk3MDE3Mjc5NDg3NjEyNDczNzc4NDUxMjkxMjE3NDg3ODQxOTc1NjI2MDczMjI0ODQ1MjI4NDM1OTk0MjI2MTg1MDc1NDI4MjA3OTg5MzAwMjExMzI5OTM4NjQxMzEwMTk5MjcwNTE0NjA2ODU5NDEzNDY1NzE1MjQyNjk0ODc0ODkwNDAzNDk5MzUxOTIxMDY4OTMwODE1ODY5ODM5NDYyMTE0MDI2MjM3MzY5OTAwMzE2MTA0NzYwMDAwNzk2NjcxOTUzNTAxMjcxMTI3MjM4NzM5NDI0Mjc2ODQyODkwNjQwNDY3NjYxNDEzODQ5Mzc5NzEwNzcxNjg0NzU4NTY1NzY2MDY4NzgwNjY0NjI3MjgiXSxbIm1hc3Rlcl9zZWNyZXQiLCI3ODg0ODQyMDE4MzA5NzY5NTg2MDY2Mjc5NDAwNjAzNjIzNjE4NzcxNTc3MjQ1NDk5NzQ5MzE0MzgyNTUyMzMzMjE4MzA3ODk1NzU4MDk2OTc3NjUzNjQ3MDcwNjk5MDE0OTY4OTUwMDg0OTk2MjMwNDAwODA4OTM0MzQ1MzQwMDcyMzY2NDg5NDYxNDg4MDk0MDgyOTk1OTU3MjUwMTg5NTkxMjg2NDQyMDg4MTMwNDA5MDA4Mjc5MzgxMjUzMDIxOTE0MTc5Nzc5MTAyNTcwNjIxNDQ0MDU0NzcxNjY5Mjk5NjQzNTcwODg5NjY1ODQzOTY2ODA1MjM1ODgxNzQ1OTQ3OTQ4NDQ2ODU1MDY4ODU4ODUzMDg2MTQ5NjMxMjA1ODcwMTIzODc1NDg3MTM0NjAxMDQwODA4Njg2MzQ4NDUwOTA0MTI4MTI4Nzk1MjUzMjczMjU3ODc4NjM4MjAxNTcyOTExMDQxNTQ4NDc0MTMzMDMyMTIyMTMyODExNjQ0NjAzNjg0MDU5MDk2ODM2NjU1NjQzMTI2NDU0NTAwNDM2MTgxNjQxMjkyNjQ4MTQ3MjYxODUyNzY5NjIyMzE5Mjk2NjI0NDU3OTg2NzI5NzMwNzE3NDEyNTE2MzEyNjQwNTM0OTE3NzEwNzE4Njc2MTMwODExMTI2NjQwMTkyODg4NjI2ODI2NTcwNzA1OTUxNTUyODI5NDY2NzY5NjUxNTcxNTI2OTMzNDUyNjY0ODk5NTExNzM0ODk0Njc3OTY5NjI0OTgzODI3MTgzMDg2NjA0NTE0NDE3MDE2MDgxNDE2Nzk0NDgwMDIwNDU2ODMxNzUyNjM2NTk1NzcwNjgwODQ0MDE0MjIyOTc2MjE5NzIzODg1MjAxNTg1ODk0MDQwMDA3MTQ5MjkwMDAxNTc4MjMxMDQiXSxbIm5hbWUiLCI4NjIxNzg3ODk1MzA5MzExOTQ3Njc0OTU5NDA4MjIzMTg5ODgwNDEzNTQ2NzIyOTYyODg5NjI0NzgwMjE1MDc4NDc3OTMxNTk5MDk5MzIwODkyNzY2NjM4NjExNDYyOTMzNTg3NjgwMTU0ODQ4ODgxMTY5MzY5NTc3OTk1NTI1ODQ2NDA1NjcyNDUyMzIyMTcyNjQ4MTc4OTEwMTg2ODkwNzYwNzM2MjMwNzA0MDA3NzU1OTA0OTIyNTUwODQ0MjkxNzgwOTk1NDAyNzUyNTU0NTAwNjg1NTY5NzYzMDc4ODY5NDU0NDI3NzY5NzU0Mzc5ODg5NzAzODQzNDM4ODcyMjMyOTc0MjIzODc5MzY5MTYzOTI1NjY3NjY5MzQyOTUwOTk5MjMwOTY1NDQ1MTkwOTM5Mzk5NzM1NjE1MTk2OTY2MzUyMTMwMzQ5MTE0NDE5OTIwMTk3NDIyMjA0ODQ2MTc2OTI3NTMwMDQ3NDkxNjI1NzAwODQ5NDc1MzQzNzk3MjU0MDYwNjc3MTA4MzkxOTU3MzU0MDAzOTAyNzMzMzEzMDI1ODE5Njk3MTIzMTc2NTg3MTU5NzQ5ODkxODg5MzU3Mjk0OTUyNDMwMDY2MTE5MjgzNzA1NTAwMTcxNTc3ODMzMzk3OTE1Mzc5OTA2NTA1MDExMjczODM2NTM3OTA2NjkyMjg2MTk5NDgzMjA3NDc1MzM2MjE1Njc4MTA1NDY5MDc3MTMyNDAwNDM4NTgyOTAyMjMwMzI5MDc4NjA4NTI4NDgzNDEwODI1NDkzNDcwMjI5MjA3MzA1NTk0ODUwMTg4ODUwMjEyNTQ0NzI0NDgyOTExOTQ3NTQ4NzIzMzMxMzYyNTI3MjA0Mzg3MzE1Mjk1MDU4Njk5MDk3NDc3MjQzMTczOTE0Njk5MDAzOTkzNTQiXV19LCJub25jZSI6IjM2MDY2NjE3NzQwNTA3NzY5NTI5NTMxNyJ9"},"format":"anoncreds/credential-offer@v1.0"}],"thid":"3a1c143b-7ab7-470d-99cf-bc5f31771388","ack":[]}"""
        )
        agent.sendMessage(message)
        verify(connectionManagerMock).sendMessage(message)
    }

    @Test
    fun testCreateNewPrismDID_shouldCreateNewDID_whenCalled() = runTest {
        val validDID = DID("did", "test", "123")
        castorMockOld.createPrismDIDReturn = validDID
        val agent = EdgeAgent(
            apollo = apolloMockOld,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMockOld,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = seed,
            api = null,
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )
        plutoMockOld.getPrismLastKeyPathIndexReturn = flow { emit(0) }
        val newDID = agent.createNewPrismDID()
        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMockOld.storedPrismDID.first())
        assertTrue { plutoMockOld.wasGetPrismLastKeyPathIndexCalled }
        assertTrue { plutoMockOld.wasStorePrismDIDAndPrivateKeysCalled }
    }

    @Test
    fun testCreateNewPeerDID_shouldCreateNewDID_whenCalled() = runTest {
        val agent = spy(
            EdgeAgent(
                apolloMock,
                castorMock,
                plutoMock,
                mercuryMockOld,
                polluxMock,
                connectionManagerMock,
                seed,
                null,
                logger = LoggerMock(),
                agentOptions = AgentOptions()
            )
        )

        val did = DID("did:peer:asdf")
        `when`(castorMock.createPeerDID(any(), any())).thenReturn(did)
        `when`(castorMock.resolveDID(any())).thenReturn(DIDDocument(did, emptyArray()))
        val newDID = agent.createNewPeerDID(services = emptyArray(), updateMediator = false)

        assertEquals(did, newDID)
    }

    @Test
    fun testCreateNewPeerDID_whenUpdateMediatorFalse_thenShouldUseProvidedServices() = runTest {
        val apollo = ApolloImpl()
        val castor = CastorImpl(apollo = apollo, logger = LoggerMock())
        val agent = EdgeAgent(
            apollo,
            castor,
            plutoMockOld,
            mercuryMockOld,
            polluxMockOld,
            connectionManagerOld,
            null,
            null,
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )

        val seAccept = arrayOf("someAccepts")
        val seRoutingKeys = arrayOf("someRoutingKey")
        val service = DIDDocument.Service(
            id = "DIDCommV2",
            type = arrayOf(DIDCOMM_MESSAGING),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "localhost:8082",
                accept = seAccept,
                routingKeys = seRoutingKeys
            )
        )
        val newDID = agent.createNewPeerDID(services = arrayOf(service), updateMediator = false)

        val document = castor.resolveDID(newDID.toString())
        val services = document.services
        assertTrue(services.isNotEmpty())
        assertEquals(service.type.first(), services.first().type.first())
        assertEquals(service.serviceEndpoint, services.first().serviceEndpoint)
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldAcceptOnboardingInvitation_whenStatusIs200() =
        runTest {
            val agent = EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
                logger = LoggerMock()
            )
            val invitationString = """
            {
                "type":"${ProtocolType.PrismOnboarding.value}",
                "onboardEndpoint":"http://localhost/onboarding",
                "from":"did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
            }
        """
            val invitation = agent.parseInvitation(invitationString)
            assertEquals(PrismOnboardingInvitation::class, invitation::class)
            agent.acceptInvitation(invitation as PrismOnboardingInvitation)
        }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldRejectOnboardingInvitation_whenStatusIsNot200() =
        runTest {
            val api = ApiMock(HttpStatusCode.BadRequest, "{\"success\":\"true\"}")
            val agent = EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = api,
                logger = LoggerMock()
            )
            val invitationString = """
            {
                "type":"${ProtocolType.PrismOnboarding.value}",
                "onboardEndpoint":"http://localhost/onboarding",
                "from":"did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
            }
        """
            val invitation = agent.parseInvitation(invitationString)
            assertFailsWith<EdgeAgentError.FailedToOnboardError> {
                agent.acceptInvitation(invitation as PrismOnboardingInvitation)
            }
        }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldRejectOnboardingInvitation_whenBodyIsWrong() =
        runTest {
            val agent = EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
                logger = LoggerMock()
            )
            val invitationString = """
            {
                "type":"${ProtocolType.PrismOnboarding.value}",
                "errorField":"http://localhost/onboarding",
                "from":"did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
            }
        """
            assertFailsWith<org.hyperledger.identus.walletsdk.domain.models.UnknownError.SomethingWentWrongError> {
                agent.parseInvitation(invitationString)
            }
        }

    @Test
    fun testPrismAgentSignWith_whenNoPrivateKeyAvailable_thenThrowCannotFindDIDPrivateKey() =
        runTest {
            val agent = EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = null,
                logger = LoggerMock()
            )

            plutoMockOld.getDIDPrivateKeysReturn = flow { emit(listOf(null)) }

            val did = DID("did", "peer", "asdf1234asdf1234")
            val messageString = "This is a message"
            assertFalse { plutoMockOld.wasGetDIDPrivateKeysByDIDCalled }
            assertFailsWith(EdgeAgentError.CannotFindDIDPrivateKey::class, null) {
                agent.signWith(did, messageString.toByteArray())
            }
        }

    @Test
    fun testPrismAgentSignWith_whenSecp256k1PrivateKey_thenSignatureReturned() = runTest {
        val apolloMock = mock<Apollo>()
        val plutoMock = mock<Pluto>()
        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMockOld,
            pollux = polluxMock,
            connectionManager = connectionManagerMock,
            seed = seed,
            api = null,
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )

        val privateKey = Secp256k1KeyPair.generateKeyPair(
            seed = seed,
            curve = KeyCurve(Curve.SECP256K1)
        ).privateKey
        val storablePrivateKeys = listOf(
            StorablePrivateKey(
                id = UUID.randomUUID().toString(),
                restorationIdentifier = "secp256k1+priv",
                data = privateKey.raw.base64UrlEncoded,
                keyPathIndex = 0
            )
        )
        // Mock getDIDPrivateKeysByDID response
        `when`(plutoMock.getDIDPrivateKeysByDID(any())).thenReturn(flow { emit(storablePrivateKeys) })
        `when`(
            apolloMock.restorePrivateKey(
                storablePrivateKeys.first().restorationIdentifier,
                storablePrivateKeys.first().data
            )
        ).thenReturn(privateKey)

        val did = DID("did", "peer", "asdf1234asdf1234")
        val messageString = "This is a message"

        assertEquals(Signature::class, agent.signWith(did, messageString.toByteArray())::class)
        verify(plutoMock).getDIDPrivateKeysByDID(any())
    }

    @Test
    fun testPrismAgentSignWith_whenEd25519PrivateKey_thenSignatureReturned() = runTest {
        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMockOld,
            pollux = polluxMock,
            connectionManager = connectionManagerMock,
            seed = seed,
            api = null,
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )

        val privateKey: Ed25519PrivateKey = Ed25519KeyPair.generateKeyPair().privateKey as Ed25519PrivateKey

        val storablePrivateKeys = listOf(
            StorablePrivateKey(
                id = UUID.randomUUID().toString(),
                restorationIdentifier = "ed25519+priv",
                data = privateKey.raw.base64UrlEncoded,
                keyPathIndex = 0
            )
        )
        `when`(plutoMock.getDIDPrivateKeysByDID(any())).thenReturn(flow { emit(storablePrivateKeys) })
        `when`(
            apolloMock.restorePrivateKey(
                storablePrivateKeys.first().restorationIdentifier,
                storablePrivateKeys.first().data
            )
        ).thenReturn(privateKey)

        val did = DID("did", "peer", "asdf1234asdf1234")
        val message = "This is a message".toByteArray()

        val expectedSignature = Signature(privateKey.sign(message))

        val signature = agent.signWith(did, message)
        assertEquals(expectedSignature, signature)
    }

    @Test
    fun testPrismAgentSignWith_whenPrivateKeyCurveNotSupported_thenThrowInvalidSpecificKeyCurve() = runTest {
        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMockOld,
            pollux = polluxMock,
            connectionManager = connectionManagerMock,
            seed = seed,
            api = null,
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )

        val privateKey = X25519KeyPair.generateKeyPair().privateKey

        val storablePrivateKeys = listOf(
            StorablePrivateKey(
                id = UUID.randomUUID().toString(),
                restorationIdentifier = "x25519+priv",
                data = privateKey.raw.base64UrlEncoded,
                keyPathIndex = 0
            )
        )
        `when`(plutoMock.getDIDPrivateKeysByDID(any())).thenReturn(flow { emit(storablePrivateKeys) })
        `when`(
            apolloMock.restorePrivateKey(
                storablePrivateKeys.first().restorationIdentifier,
                storablePrivateKeys.first().data
            )
        ).thenReturn(privateKey)

        val did = DID("did", "peer", "asdf1234asdf1234")
        val message = "This is a message".toByteArray()

        assertFailsWith(ApolloError.InvalidKeyCurve::class) {
            agent.signWith(did, message)
        }
    }

    @Test
    fun testParseInvitation_whenOutOfBand_thenReturnsOutOfBandInvitationObject() = runTest {
        val agent = EdgeAgent(
            apollo = apolloMockOld,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMockOld,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = null,
            api = null,
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )

        val invitationString = """
            {
              "type": "https://didcomm.org/out-of-band/2.0/invitation",
              "id": "1234-1234-1234-1234",
              "from": "did:peer:asdf42sf",
              "body": {
                "goal_code": "issue-vc",
                "goal": "To issue a Faber College Graduate credential",
                "accept": [
                  "didcomm/v2",
                  "didcomm/aip2;env=rfc587"
                ]
              }
            }
        """

        val invitation = agent.parseInvitation(invitationString.trim())
        assert(invitation is OutOfBandInvitation)
        val oobInvitation: OutOfBandInvitation = invitation as OutOfBandInvitation
        assertEquals("https://didcomm.org/out-of-band/2.0/invitation", oobInvitation.type.value)
        assertEquals(DID("did:peer:asdf42sf").toString(), oobInvitation.from)
        assertEquals(
            OutOfBandInvitation.Body(
                "issue-vc",
                "To issue a Faber College Graduate credential",
                listOf("didcomm/v2", "didcomm/aip2;env=rfc587")
            ),
            oobInvitation.body
        )
    }

    @Test
    fun testParseInvitation_whenOutOfBandWrongBody_thenThrowsUnknownInvitationTypeError() =
        runTest {
            val agent = EdgeAgent(
                apollo = apolloMockOld,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMockOld,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = null,
                logger = LoggerMock()
            )

            val invitationString = """
            {
              "type": "https://didcomm.org/out-of-band/2.0/invitation",
              "id": "1234-1234-1234-1234",
              "from": "did:peer:asdf42sf",
              "wrongBody": {}
            }
        """

            assertFailsWith<SerializationException> {
                agent.parseInvitation(invitationString.trim())
            }
        }

    @Test
    fun testStartPrismAgent_whenCalled_thenStatusIsRunning() = runTest {
        val getLinkSecretReturn = flow<String> { emit("linkSecret") }
        plutoMockOld.getLinkSecretReturn = getLinkSecretReturn
        val agent = EdgeAgent(
            apollo = apolloMockOld,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMockOld,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = null,
            api = null,
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )
        assertEquals(EdgeAgent.State.STOPPED, agent.state)
        agent.start()
        assertEquals(EdgeAgent.State.RUNNING, agent.state)
    }

    @Test
    fun testStopPrismAgent_whenCalled_thenStatusIsStopped() = runTest {
        val agent = EdgeAgent(
            apollo = apolloMockOld,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMockOld,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = null,
            api = null,
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )
        agent.stop()
        assertEquals(EdgeAgent.State.STOPPED, agent.state)
    }

    @Test
    fun test_OOPInvitationInURLFormat() = runTest {
        val oob =
            "https://my.domain.com/path?_oob=eyJpZCI6ImQzNjM3NzlhLWYyMmItNGFiNC1hYjY0LTkxZjkxNjgzNzYwNyIsInR5cGUiOiJodHRwczovL2RpZGNvbW0ub3JnL291dC1vZi1iYW5kLzIuMC9pbnZpdGF0aW9uIiwiZnJvbSI6ImRpZDpwZWVyOjIuRXo2TFNjcGZReGJ2VEhLaGpvbzVvMzlmc254VEp1RTRobVp3ckROUE5BVzI0dmFORi5WejZNa3UzSkpVTDNkaHpYQXB0RWpuUDFpNkF0TDlTNGlwRTNYOHM3MWV4MW9WVGNHLlNleUowSWpvaVpHMGlMQ0p6SWpvaWFIUjBjSE02THk5ck9ITXRaR1YyTG1GMFlXeGhjSEpwYzIwdWFXOHZjSEpwYzIwdFlXZGxiblF2Wkdsa1kyOXRiU0lzSW5JaU9sdGRMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDAiLCJib2R5Ijp7ImdvYWxfY29kZSI6ImlvLmF0YWxhcHJpc20uY29ubmVjdCIsImdvYWwiOiJFc3RhYmxpc2ggYSB0cnVzdCBjb25uZWN0aW9uIGJldHdlZW4gdHdvIHBlZXJzIHVzaW5nIHRoZSBwcm90b2NvbCAnaHR0cHM6Ly9hdGFsYXByaXNtLmlvL21lcmN1cnkvY29ubmVjdGlvbnMvMS4wL3JlcXVlc3QnIiwiYWNjZXB0IjpbXX19"
        val agent = EdgeAgent(
            apollo = apolloMockOld,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMockOld,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = null,
            api = null,
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )
        val x = agent.parseInvitation(oob)
        assert(x is OutOfBandInvitation)
        assert((x as OutOfBandInvitation).type == ProtocolType.Didcomminvitation)
    }

    // Commented out as it should be moved to instrumentation tests to be albe to test the anoncreds-rs library
//    @AndroidIgnore
//    @Test
//    fun testPrepareRequestCredentialWithIssuer_whenAnoncredOfferCredential_thenProcessed() = runTest {
//        val apiMock: Api = ApiMock(
//            HttpStatusCode(200, "Ok"),
//            getCredentialDefinitionResponse
//        )
//        val pollux = PolluxImpl(apolloMockOld, castorMockOld, apiMock)
//        plutoMockOld.getLinkSecretReturn = flow { emit(LinkSecret().getValue()) }
//
//        val agent = EdgeAgent(
//            apollo = apolloMockOld,
//            castor = castorMockOld,
//            pluto = plutoMockOld,
//            mercury = mercuryMock,
//            pollux = pollux,
//            connectionManager = connectionManagerOld,
//            seed = null,
//            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
//            logger = PrismLoggerMock(),
//            agentOptions = AgentOptions()
//        )
//
//        val message = Json.decodeFromString<Message>(
//            """{"id":"e430e4af-455e-4a15-9f2f-5bd8e5f350b8","piuri":"https://didcomm.org/issue-credential/3.0/offer-credential","from":{"method":"peer","methodId":"2.Ez6LSm5hETc4CS4X8RxYYKjoS2B3CM8TyzbgRrE7kGrdymHdq.Vz6MkoP2VXs4N7iNsKTzEKtZbnfu6yDH1x2ajGtCmNmc6qdMW.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},"to":{"method":"peer","methodId":"2.Ez6LSd8irQSWjjMfvg53kcaDY6Q2doPEvQwscjSzidgWoFUVK.Vz6Mksu4QVe8oKwJEDPgxRg2bFa3QWrZR1EZGC9xq8xk9twYX.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},"fromPrior":"null","body":"{\"multiple_available\":null,\"goal_code\":\"Offer Credential\",\"credential_preview\":{\"schema_id\":\"http:\\/\\/192.168.68.113:8000\\/cloud-agent\\/schema-registry\\/schemas\\/5667190d-640c-36af-a9f1-f4ed2587e766\\/schema\",\"type\":\"https:\\/\\/didcomm.org\\/issue-credential\\/3.0\\/credential-credential\",\"body\":{\"attributes\":[{\"media_type\":null,\"name\":\"age\",\"value\":\"18\"},{\"media_type\":null,\"name\":\"name\",\"value\":\"Cristian\"}]}},\"replacement_id\":null,\"comment\":null}","created_time":"1721242264","expires_time_plus":"1721328667","attachments":[{"id":"ee903fe0-2c49-4356-9b41-cfccc979c0a1","data":{"base64":"eyJzY2hlbWFfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9jbG91ZC1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy81NjY3MTkwZC02NDBjLTM2YWYtYTlmMS1mNGVkMjU4N2U3NjYvc2NoZW1hIiwiY3JlZF9kZWZfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9jbG91ZC1hZ2VudC9jcmVkZW50aWFsLWRlZmluaXRpb24tcmVnaXN0cnkvZGVmaW5pdGlvbnMvM2RhNmExZDgtMmIyMy0zMTM4LWIwMmEtYWIwYmI0OGY4MGY5L2RlZmluaXRpb24iLCJrZXlfY29ycmVjdG5lc3NfcHJvb2YiOnsiYyI6IjM0MjE0MzM4OTA5MDk4MTU5ODIyNTA3MjQ4Njk3NjUyNTIyMzc1NzM2ODM0OTM1MDg0NjM5MTYzNTUyNjgwMjc3MjQyOTcxODI5ODUwIiwieHpfY2FwIjoiODAwNzA3NDcxNzUyOTQ3MTI1NDIxNTU2ODI1ODYyNDc5NzE1OTE0OTE3ODE4NTY4MDI1NjU5Mzk1NDcyMTAzMzQ0NjAwMDI4NzU4MzczMTA5Mjg0NzEzNDg5MDg1OTk3NzE5NDcwOTc5MDQ1NzA3NTY1ODA4MDczNzYxMjI0OTI3MzcyMDk1MTU2MzAxODE3MzgzNDE5NzE4NzM0NDk2MDQwMjc1MzM2MTg0ODIzNzQ4NTg0NzgzNjIxNzE1NTQwOTI3Nzk0ODQyMTA2NDM1NDEwNzc4MDg2ODg1MTc5MzQzMjEzMjU3NTk2ODM2NjU5NzYwMTI4NzI5ODI5MTk2MzI0MjQwMzgyOTc3MzczNjU3MTA0NjQ5NjE4MjU0MDMzMDk4Njc0OTkxMzIwMzc2NTEyMTUyOTk3Mzg0Mzk5MzY0OTc3MDM4NzU2ODcwOTU3NTcyMDM0NTM0NTY1MDM5OTY0MTYzNDgzNDEyMTEzNzc0NzA5MTU2NTcxMjcxNTI3ODY2NzQwNzU1Nzc2MzIzOTgxNTE5NzEzNjQ1MjQ4Njc5NzgyOTM3NTcyOTI4NDI1NzQ0NjE3MjMwNzk2MzYwNzE5MDA3Mzc0ODgwNTI2ODA4ODIzODg2NTMwMzE2MzgyOTI1MDcxOTYzMjUwMzgyNzU4MDA5Mjk1MzI1NDgzODIyNTg0OTIyMDkwMTcwMTAxOTY5OTk5MTg1MzAxMDA1NDM2MjYyMDI4NDIxOTA0NjU5MzY4OTU3OTQyNTAwNDkxNzI5MjY0MTMzMzE5Nzg0NDIwNjQ0NDUxMTUyNjA2MDI2MDg1MDkwNDYzNzAzMTQzNDczMTcxMjg3OTUxMTM0NTM1MDY0NDc0NjQ5NDE5MzUzMDQ4MzQwOTk4MTY2NjYwNDkyMDE5ODQyNDU2NDM5MTcxNjM2IiwieHJfY2FwIjpbWyJhZ2UiLCI3MjA2NjI4MDAzNzAwMjM5MzM5NjAzNjUxNDQ1MTY2NDE0MjcwNTA0ODM4ODAwMzYxMjE0NjQ3MjA1ODYwOTczMTg4MTQ0OTE2OTkwOTE3NTIyNjU4Mzg3Njg1MTcwMzkyMDcyMjk0MTUxMDUwODk1Njc0MTU0OTYwMDMyMDM0Mzg2MzEzMzY0MTMwMDQwNzI5ODQwMjcxMzcyNjc4NTI0NjE0NjUxNDcxNDU0NTg4MzgwMzI2OTMwODQ0Njc4Nzg3MzA3NzY3ODk2NjY5NTE2MDY1Njc3MDA3MjQyMDEwMjQyMDAwNTg4NjgxMTczNDUxNjg4NTU5MDEwMjQyNTgyMTg4MTY5MTUyNjUwNzY4NzgxNjMyMjgwNTgyODI1NjM3MjY0NzUwMTA3NTU2NDQzNTgyNzMwNDIxMjE3NTI4OTgyNTE5MzA3NzQ0ODAxNTYyNTYyMzQzNTcyNzU4NDEzNjc1NzY0ODQwOTY5MTY3NTE3ODcyNjk2MDY1MDM2MDU1MzgwMDg2NjcyNjUzMDEyMTIxMDk2MTA5OTQyMTg1NjM2ODk3MDE3Mjc5NDg3NjEyNDczNzc4NDUxMjkxMjE3NDg3ODQxOTc1NjI2MDczMjI0ODQ1MjI4NDM1OTk0MjI2MTg1MDc1NDI4MjA3OTg5MzAwMjExMzI5OTM4NjQxMzEwMTk5MjcwNTE0NjA2ODU5NDEzNDY1NzE1MjQyNjk0ODc0ODkwNDAzNDk5MzUxOTIxMDY4OTMwODE1ODY5ODM5NDYyMTE0MDI2MjM3MzY5OTAwMzE2MTA0NzYwMDAwNzk2NjcxOTUzNTAxMjcxMTI3MjM4NzM5NDI0Mjc2ODQyODkwNjQwNDY3NjYxNDEzODQ5Mzc5NzEwNzcxNjg0NzU4NTY1NzY2MDY4NzgwNjY0NjI3MjgiXSxbIm1hc3Rlcl9zZWNyZXQiLCI3ODg0ODQyMDE4MzA5NzY5NTg2MDY2Mjc5NDAwNjAzNjIzNjE4NzcxNTc3MjQ1NDk5NzQ5MzE0MzgyNTUyMzMzMjE4MzA3ODk1NzU4MDk2OTc3NjUzNjQ3MDcwNjk5MDE0OTY4OTUwMDg0OTk2MjMwNDAwODA4OTM0MzQ1MzQwMDcyMzY2NDg5NDYxNDg4MDk0MDgyOTk1OTU3MjUwMTg5NTkxMjg2NDQyMDg4MTMwNDA5MDA4Mjc5MzgxMjUzMDIxOTE0MTc5Nzc5MTAyNTcwNjIxNDQ0MDU0NzcxNjY5Mjk5NjQzNTcwODg5NjY1ODQzOTY2ODA1MjM1ODgxNzQ1OTQ3OTQ4NDQ2ODU1MDY4ODU4ODUzMDg2MTQ5NjMxMjA1ODcwMTIzODc1NDg3MTM0NjAxMDQwODA4Njg2MzQ4NDUwOTA0MTI4MTI4Nzk1MjUzMjczMjU3ODc4NjM4MjAxNTcyOTExMDQxNTQ4NDc0MTMzMDMyMTIyMTMyODExNjQ0NjAzNjg0MDU5MDk2ODM2NjU1NjQzMTI2NDU0NTAwNDM2MTgxNjQxMjkyNjQ4MTQ3MjYxODUyNzY5NjIyMzE5Mjk2NjI0NDU3OTg2NzI5NzMwNzE3NDEyNTE2MzEyNjQwNTM0OTE3NzEwNzE4Njc2MTMwODExMTI2NjQwMTkyODg4NjI2ODI2NTcwNzA1OTUxNTUyODI5NDY2NzY5NjUxNTcxNTI2OTMzNDUyNjY0ODk5NTExNzM0ODk0Njc3OTY5NjI0OTgzODI3MTgzMDg2NjA0NTE0NDE3MDE2MDgxNDE2Nzk0NDgwMDIwNDU2ODMxNzUyNjM2NTk1NzcwNjgwODQ0MDE0MjIyOTc2MjE5NzIzODg1MjAxNTg1ODk0MDQwMDA3MTQ5MjkwMDAxNTc4MjMxMDQiXSxbIm5hbWUiLCI4NjIxNzg3ODk1MzA5MzExOTQ3Njc0OTU5NDA4MjIzMTg5ODgwNDEzNTQ2NzIyOTYyODg5NjI0NzgwMjE1MDc4NDc3OTMxNTk5MDk5MzIwODkyNzY2NjM4NjExNDYyOTMzNTg3NjgwMTU0ODQ4ODgxMTY5MzY5NTc3OTk1NTI1ODQ2NDA1NjcyNDUyMzIyMTcyNjQ4MTc4OTEwMTg2ODkwNzYwNzM2MjMwNzA0MDA3NzU1OTA0OTIyNTUwODQ0MjkxNzgwOTk1NDAyNzUyNTU0NTAwNjg1NTY5NzYzMDc4ODY5NDU0NDI3NzY5NzU0Mzc5ODg5NzAzODQzNDM4ODcyMjMyOTc0MjIzODc5MzY5MTYzOTI1NjY3NjY5MzQyOTUwOTk5MjMwOTY1NDQ1MTkwOTM5Mzk5NzM1NjE1MTk2OTY2MzUyMTMwMzQ5MTE0NDE5OTIwMTk3NDIyMjA0ODQ2MTc2OTI3NTMwMDQ3NDkxNjI1NzAwODQ5NDc1MzQzNzk3MjU0MDYwNjc3MTA4MzkxOTU3MzU0MDAzOTAyNzMzMzEzMDI1ODE5Njk3MTIzMTc2NTg3MTU5NzQ5ODkxODg5MzU3Mjk0OTUyNDMwMDY2MTE5MjgzNzA1NTAwMTcxNTc3ODMzMzk3OTE1Mzc5OTA2NTA1MDExMjczODM2NTM3OTA2NjkyMjg2MTk5NDgzMjA3NDc1MzM2MjE1Njc4MTA1NDY5MDc3MTMyNDAwNDM4NTgyOTAyMjMwMzI5MDc4NjA4NTI4NDgzNDEwODI1NDkzNDcwMjI5MjA3MzA1NTk0ODUwMTg4ODUwMjEyNTQ0NzI0NDgyOTExOTQ3NTQ4NzIzMzMxMzYyNTI3MjA0Mzg3MzE1Mjk1MDU4Njk5MDk3NDc3MjQzMTczOTE0Njk5MDAzOTkzNTQiXV19LCJub25jZSI6IjM2MDY2NjE3NzQwNTA3NzY5NTI5NTMxNyJ9"},"format":"anoncreds/credential-offer@v1.0"}],"thid":"3a1c143b-7ab7-470d-99cf-bc5f31771388","ack":[]}"""
//        )
//        val offerCredential = OfferCredential.fromMessage(message)
//        val subjectDid =
//            DID("did:prism:6f23ddace519b68dfc0fa06e992db40f2f3c584af382ce446fa2fd0e042e5dea:CoUBCoIBEjsKB21hc3RlcjAQAUouCglzZWNwMjU2azESIQMcKwEitGbQKtGa-jFXi3m1u7OP2JMukYXQnZk3fQIXvxJDCg9hdXRoZW50aWNhdGlvbjAQBEouCglzZWNwMjU2azESIQMcKwEitGbQKtGa-jFXi3m1u7OP2JMukYXQnZk3fQIXvw")
//
//        val requestCredential =
//            agent.prepareRequestCredentialWithIssuer(did = subjectDid, offer = offerCredential)
//
//        assertEquals(offerCredential.from, requestCredential.to)
//        assertEquals(offerCredential.to, requestCredential.from)
//        assertTrue(requestCredential.attachments.size == 1)
//        assertEquals(requestCredential.attachments[0].format, CredentialType.ANONCREDS_REQUEST.type)
//        assertEquals(offerCredential.thid, requestCredential.thid)
//    }

    @Test
    fun testPrepareRequestCredentialWithIssuer_whenJwtOfferCredential_thenProcessed() = runTest {
        val seed =
            Seed("Rb8j6NVmA120auCQT6tP35rZ6-hgHvhcZCYmKmU1Avc4b5Tc7XoPeDdSWZYjLXuHn4w0f--Ulm1WkU1tLzwUEA".base64UrlDecodedBytes)
        val agent = EdgeAgent(
            apollo = apolloMockOld,
            castor = castorMockOld,
            pluto = plutoMock,
            mercury = mercuryMockOld,
            pollux = polluxMock,
            connectionManager = connectionManagerOld,
            seed = seed,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )

        `when`(plutoMock.getPrismDIDKeyPathIndex(any())).thenReturn(flow { emit(2) })

        `when`(polluxMock.extractCredentialFormatFromMessage(any())).thenReturn(CredentialType.JWT)

        val jwtString =
            """eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MGE0YjU1MjE2OWUzMTU4NzgxNzQxZmJiZWZmZTgxMjEyNzg0ZDMyZDkwY2Y4ZjI2MjI5MjNmMTFmNmVjZDk2NjpDb1VCQ29JQkVqc0tCMjFoYzNSbGNqQVFBVW91Q2dselpXTndNalUyYXpFU0lRTGd6aHN1T3FoQXlJbXktYzhvOVptSUo0aVlfR2M4dHZOSVQzbDF3NThmMkJKRENnOWhkWFJvWlc1MGFXTmhkR2x2YmpBUUJFb3VDZ2x6WldOd01qVTJhekVTSVFMZ3poc3VPcWhBeUlteS1jOG85Wm1JSjRpWV9HYzh0dk5JVDNsMXc1OGYyQSIsImF1ZCI6ImRvbWFpbiIsInZwIjp7IkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwidHlwZSI6WyJWZXJpZmlhYmxlUHJlc2VudGF0aW9uIl19LCJub25jZSI6ImVhNzBmMzIwLTgwYjYtNGE5My1iMjkzLTJmNjE2NWRmNmRlYyJ9.iAQ3iwiz9-0fCO28TPGUG0y8y1xN9mgTYPTud8sL_p8Qrz8MwtS2NCvl_CR-vCnh1jzi5AJIo99AbFkGYkZ6XQ"""
        `when`(polluxMock.processCredentialRequestJWT(any(), any(), any())).thenReturn(jwtString)

        val message = Json.decodeFromString<Message>(
            """{"id":"a77e5336-f4bb-4ccb-ae6f-2929ed53bbc3","piuri":"https://didcomm.org/issue-credential/3.0/offer-credential","from":{"method":"peer","methodId":"2.Ez6LSm5hETc4CS4X8RxYYKjoS2B3CM8TyzbgRrE7kGrdymHdq.Vz6MkoP2VXs4N7iNsKTzEKtZbnfu6yDH1x2ajGtCmNmc6qdMW.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},"to":{"method":"peer","methodId":"2.Ez6LSd8irQSWjjMfvg53kcaDY6Q2doPEvQwscjSzidgWoFUVK.Vz6Mksu4QVe8oKwJEDPgxRg2bFa3QWrZR1EZGC9xq8xk9twYX.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},"fromPrior":"null","body":"{\"multiple_available\":null,\"goal_code\":\"Offer Credential\",\"credential_preview\":{\"schema_id\":null,\"type\":\"https:\\/\\/didcomm.org\\/issue-credential\\/3.0\\/credential-credential\",\"body\":{\"attributes\":[{\"media_type\":null,\"name\":\"familyName\",\"value\":\"demo\"},{\"media_type\":null,\"name\":\"drivingClass\",\"value\":\"1\"},{\"media_type\":null,\"name\":\"dateOfIssuance\",\"value\":\"01\\/01\\/2024\"},{\"media_type\":null,\"name\":\"emailAddress\",\"value\":\"demo@email.com\"},{\"media_type\":null,\"name\":\"drivingLicenseID\",\"value\":\"A1221332\"}]}},\"replacement_id\":null,\"comment\":null}","created_time":"1721242627","expires_time_plus":"1721329026","attachments":[{"id":"d2c693e7-c217-46e5-92ba-6a0880a89416","data":{"data":"{\"options\":{\"domain\":\"domain\",\"challenge\":\"ea70f320-80b6-4a93-b293-2f6165df6dec\"},\"presentation_definition\":{\"purpose\":null,\"format\":{\"jwt\":{\"proof_type\":[],\"alg\":[\"ES256K\"]},\"ldp\":null},\"name\":null,\"input_descriptors\":[],\"id\":\"aa81d240-9d95-4f2c-bc94-09101c2081c0\"}}"},"format":"prism/jwt"}],"thid":"8b58db48-d243-45b8-a0f1-9862323b7e77","ack":[]}"""
        )
        val offerCredential = OfferCredential.fromMessage(message)
        val subjectDID = DID("did:prism:asdf42sf")

        val requestCredential =
            agent.prepareRequestCredentialWithIssuer(did = subjectDID, offer = offerCredential)

        assertEquals(offerCredential.from, requestCredential.to)
        assertEquals(offerCredential.to, requestCredential.from)
        assertTrue(requestCredential.attachments.size == 1)
        assertEquals(requestCredential.attachments[0].format, CredentialType.JWT.type)
        assertTrue(requestCredential.attachments[0].data is AttachmentBase64)
        assertEquals(offerCredential.thid, requestCredential.thid)
    }

    @Test
    fun testPrepareRequestCredentialWithIssuer_whenDIDNotPrism_thenThrowException() = runTest {
        val seed =
            Seed("Rb8j6NVmA120auCQT6tP35rZ6-hgHvhcZCYmKmU1Avc4b5Tc7XoPeDdSWZYjLXuHn4w0f--Ulm1WkU1tLzwUEA".base64UrlDecodedBytes)
        val agent = EdgeAgent(
            apollo = apolloMockOld,
            castor = castorMockOld,
            pluto = plutoMock,
            mercury = mercuryMockOld,
            pollux = polluxMock,
            connectionManager = connectionManagerOld,
            seed = seed,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )

        val message = Json.decodeFromString<Message>(
            """{"id":"a77e5336-f4bb-4ccb-ae6f-2929ed53bbc3","piuri":"https://didcomm.org/issue-credential/3.0/offer-credential","from":{"method":"peer","methodId":"2.Ez6LSm5hETc4CS4X8RxYYKjoS2B3CM8TyzbgRrE7kGrdymHdq.Vz6MkoP2VXs4N7iNsKTzEKtZbnfu6yDH1x2ajGtCmNmc6qdMW.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},"to":{"method":"peer","methodId":"2.Ez6LSd8irQSWjjMfvg53kcaDY6Q2doPEvQwscjSzidgWoFUVK.Vz6Mksu4QVe8oKwJEDPgxRg2bFa3QWrZR1EZGC9xq8xk9twYX.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},"fromPrior":"null","body":"{\"multiple_available\":null,\"goal_code\":\"Offer Credential\",\"credential_preview\":{\"schema_id\":null,\"type\":\"https:\\/\\/didcomm.org\\/issue-credential\\/3.0\\/credential-credential\",\"body\":{\"attributes\":[{\"media_type\":null,\"name\":\"familyName\",\"value\":\"demo\"},{\"media_type\":null,\"name\":\"drivingClass\",\"value\":\"1\"},{\"media_type\":null,\"name\":\"dateOfIssuance\",\"value\":\"01\\/01\\/2024\"},{\"media_type\":null,\"name\":\"emailAddress\",\"value\":\"demo@email.com\"},{\"media_type\":null,\"name\":\"drivingLicenseID\",\"value\":\"A1221332\"}]}},\"replacement_id\":null,\"comment\":null}","created_time":"1721242627","expires_time_plus":"1721329026","attachments":[{"id":"d2c693e7-c217-46e5-92ba-6a0880a89416","data":{"data":"{\"options\":{\"domain\":\"domain\",\"challenge\":\"ea70f320-80b6-4a93-b293-2f6165df6dec\"},\"presentation_definition\":{\"purpose\":null,\"format\":{\"jwt\":{\"proof_type\":[],\"alg\":[\"ES256K\"]},\"ldp\":null},\"name\":null,\"input_descriptors\":[],\"id\":\"aa81d240-9d95-4f2c-bc94-09101c2081c0\"}}"},"format":"prism/jwt"}],"thid":"8b58db48-d243-45b8-a0f1-9862323b7e77","ack":[]}"""
        )
        val offerCredential = OfferCredential.fromMessage(message)
        val subjectDID = DID("did:peer:asdf42sf")

        assertFailsWith(PolluxError.InvalidPrismDID::class) {
            agent.prepareRequestCredentialWithIssuer(did = subjectDID, offer = offerCredential)
        }
    }

    @Test
    fun testPrepareRequestCredentialWithIssuer_whenCredentialTypeNotSupported_thenThrowException() = runTest {
        val seed =
            Seed("Rb8j6NVmA120auCQT6tP35rZ6-hgHvhcZCYmKmU1Avc4b5Tc7XoPeDdSWZYjLXuHn4w0f--Ulm1WkU1tLzwUEA".base64UrlDecodedBytes)
        val agent = EdgeAgent(
            apollo = apolloMockOld,
            castor = castorMockOld,
            pluto = plutoMock,
            mercury = mercuryMockOld,
            pollux = polluxMock,
            connectionManager = connectionManagerOld,
            seed = seed,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )

        `when`(plutoMock.getPrismDIDKeyPathIndex(any())).thenReturn(flow { emit(2) })

        `when`(polluxMock.extractCredentialFormatFromMessage(any())).thenReturn(CredentialType.PRESENTATION_EXCHANGE_SUBMISSION)

        val message = Json.decodeFromString<Message>(
            """{"id":"a77e5336-f4bb-4ccb-ae6f-2929ed53bbc3","piuri":"https://didcomm.org/issue-credential/3.0/offer-credential","from":{"method":"peer","methodId":"2.Ez6LSm5hETc4CS4X8RxYYKjoS2B3CM8TyzbgRrE7kGrdymHdq.Vz6MkoP2VXs4N7iNsKTzEKtZbnfu6yDH1x2ajGtCmNmc6qdMW.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},"to":{"method":"peer","methodId":"2.Ez6LSd8irQSWjjMfvg53kcaDY6Q2doPEvQwscjSzidgWoFUVK.Vz6Mksu4QVe8oKwJEDPgxRg2bFa3QWrZR1EZGC9xq8xk9twYX.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},"fromPrior":"null","body":"{\"multiple_available\":null,\"goal_code\":\"Offer Credential\",\"credential_preview\":{\"schema_id\":null,\"type\":\"https:\\/\\/didcomm.org\\/issue-credential\\/3.0\\/credential-credential\",\"body\":{\"attributes\":[{\"media_type\":null,\"name\":\"familyName\",\"value\":\"demo\"},{\"media_type\":null,\"name\":\"drivingClass\",\"value\":\"1\"},{\"media_type\":null,\"name\":\"dateOfIssuance\",\"value\":\"01\\/01\\/2024\"},{\"media_type\":null,\"name\":\"emailAddress\",\"value\":\"demo@email.com\"},{\"media_type\":null,\"name\":\"drivingLicenseID\",\"value\":\"A1221332\"}]}},\"replacement_id\":null,\"comment\":null}","created_time":"1721242627","expires_time_plus":"1721329026","attachments":[{"id":"d2c693e7-c217-46e5-92ba-6a0880a89416","data":{"data":"{\"options\":{\"domain\":\"domain\",\"challenge\":\"ea70f320-80b6-4a93-b293-2f6165df6dec\"},\"presentation_definition\":{\"purpose\":null,\"format\":{\"jwt\":{\"proof_type\":[],\"alg\":[\"ES256K\"]},\"ldp\":null},\"name\":null,\"input_descriptors\":[],\"id\":\"aa81d240-9d95-4f2c-bc94-09101c2081c0\"}}"},"format":"prism/jwt"}],"thid":"8b58db48-d243-45b8-a0f1-9862323b7e77","ack":[]}"""
        )
        val offerCredential = OfferCredential.fromMessage(message)
        val subjectDID = DID("did:prism:asdf42sf")

        assertFailsWith(EdgeAgentError.InvalidCredentialError::class) {
            agent.prepareRequestCredentialWithIssuer(did = subjectDID, offer = offerCredential)
        }
    }

    @AndroidIgnore
    @Test
    fun testAnoncreds_whenCredentialIssued_thenProcessed() = runTest {
        val fromDID = DID("did:prism:asdf42sf")
        val toDID = DID("did:prism:asdf42sf")

        val apiMock: Api = ApiMock(
            HttpStatusCode(200, "Ok"),
            getCredentialDefinitionResponse
        )
        val pollux = PolluxImpl(apolloMockOld, castorMockOld, apiMock)
        plutoMockOld.getLinkSecretReturn = flow { emit(createLinkSecret()) }
        val meta = CredentialRequestMeta(
            linkSecretName = "1",
            json = "{\"link_secret_blinding_data\":{\"v_prime\":\"1234\",\"vr_prime\":\"1234\"},\"nonce\":\"411729288962137159046778\",\"link_secret_name\":\"link:secret:id\"}"
        )
        plutoMockOld.getCredentialMetadataReturn = flow { emit(meta) }

        val agent = EdgeAgent(
            apollo = apolloMockOld,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMockOld,
            pollux = pollux,
            connectionManager = connectionManagerOld,
            seed = null,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = LoggerMock(),
            agentOptions = AgentOptions()
        )

        val attachmentDescriptor =
            AttachmentDescriptor(
                mediaType = "application/json",
                format = CredentialType.ANONCREDS_ISSUE.type,
                data = AttachmentBase64(
                    "eyJzY2hlbWFfaWQiOiJodHRwOi8vaG9zdC5kb2NrZXIuaW50ZXJuYWw6ODAwMC9wcmlzbS1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy81ZTBkNWE5My00YmZkLTMxMTEtYTk1Ni01ZDViYzgyZjc2Y2MiLCJjcmVkX2RlZl9pZCI6Imh0dHA6Ly8xOTIuMTY4LjY4LjEwMjo4MDAwL3ByaXNtLWFnZW50L2NyZWRlbnRpYWwtZGVmaW5pdGlvbi1yZWdpc3RyeS9kZWZpbml0aW9ucy81ZTI5NWNmMi00MjI2LTNjN2QtYjI3Zi04N2JlMTYwNjQ3NmQvZGVmaW5pdGlvbiIsInJldl9yZWdfaWQiOm51bGwsInZhbHVlcyI6eyJkcml2aW5nTGljZW5zZUlEIjp7InJhdyI6IjEyMzQ1IiwiZW5jb2RlZCI6IjEyMzQ1In0sImVtYWlsQWRkcmVzcyI6eyJyYXciOiJhbGljZUB3b25kZXJsYW5kLmNvbSIsImVuY29kZWQiOiI3NTEwNzA0NjM0MDE2NTY3NzAxMTk0MjA3NTY0NDAwOTA2NjU0MTY2MTE0ODgyMjU4OTAzMzY3OTg2MTIwOTE4NjQ5Mjc2ODY2OTkyNCJ9LCJkYXRlT2ZJc3N1YW5jZSI6eyJyYXciOiIyMDIwLTExLTEzVDIwOjIwOjM5KzAwOjAwIiwiZW5jb2RlZCI6IjUzODY4NTU5NTMxNzQ4NDY3MDk3NTIwODU5MDUzMDgxODM1NzQ3NDM1NjExNjA2ODQyMDQxMTQ3NTgyMDA0ODc0MzQ4MDQ2MTY0Njg1In0sImZhbWlseU5hbWUiOnsicmF3IjoiV29uZGVybGFuZCIsImVuY29kZWQiOiIxNjc5MDg0OTMxMjM3NDc5NDczNjgxMzM3NzU2NzI1Mzg1MTM3MzYwNzk3MDQ3MzM3NzcwMTQ3NzI2OTE5NDAxOTU1NzY1NDU2MjAzNSJ9LCJkcml2aW5nQ2xhc3MiOnsicmF3IjoiMyIsImVuY29kZWQiOiIzIn19LCJzaWduYXR1cmUiOnsicF9jcmVkZW50aWFsIjp7Im1fMiI6IjExMTI0OTQwODk5NTQ2MzQ0NTY2MjA3NjExNTQyODEwMzAyNDE1ODY5Njg4MDE3NTMyNDYwMzM3MDY0NTk2Mjk1NjIxNzkwMDk2MTUxNSIsImEiOiIxNjk5NjYwOTQ5MDE3NjQ2MzAzOTIxMTc4MjgyNDkxODM5MjE4MzUwNjk5MjM4Nzk2NDQxOTg4OTI0NzA1MzI0NjMzNTQ0MzM0Mzc1ODQwNjgyODY3Nzc3MDQ5NDY2ODM3NjcyMjQxMzgxMjgwMjE0OTIxMDI1NDcyNTAwNDAwMjYyMjA1NTE1MzQxOTQxOTEyMjc2ODIxMTcwNzYwNDAwNzkzMzA3NDY3MDQzOTAzMTAzMDI3MTI2NjU4MDM3MDg1MTExMDU1NDczOTI2MTc2ODU2ODY1MTg3Njc3OTAzMTQzODkzNzg2NTU0NjA2MDQ1MDYyMDQxMzAxMzM3MDQ0NTQzMTk2NTM0NzMwNjczODc4Njg3MjUxMjc3NzEyMzEwODUzNDU4MTYxMjg1NzE1MDIxNTk4OTkxODgxNjE5OTI0NTU5NzY0NTU3NDQzMjUxMDcwNzg5NDAzMTQ2MzMxODE4Njk4NTU1MzIzMDU0NTUzNDI0NzkyNjk5OTE2MzIzODg2NzA0MjE0NDY0ODcwMTI5NTkwODcwODIxOTc1MDkwNjQ4MTkzNjExNjY5MjMxMTk2NzY2Nzg5ODU5NDQ3MTMwOTMwNDIxMjI0NTE0MTI1ODYyMzI5OTU5NTIyNjkxMTE0Mzc3MzA4MzYyNjY2OTY2NzQ3NTQxNjU2NzkzOTc4ODc4Mzc0NTQ4MzcwODgyMDMzMTk2MDQxMjE5NDQ1MjAxOTIxNDYzNzEwMjM4OTUyMjkzNDkyODg0OTU5MDgxMDU3MDc2NzAwOTI1OTI2NTYyODg4MTcwODk5NzIzNDA2MjMzMzc0OSIsImUiOiIyNTkzNDQ3MjMwNTUwNjIwNTk5MDcwMjU0OTE0ODA2OTc1NzE5MzgyNzc4ODk1MTUxNTIzMDYyNDk3Mjg1ODMxMDU2NjU4MDA3MTMzMDY3NTkxNDk5ODE2OTA1NTkxOTM5ODcxNDMwMTIzNjc5MTMyMDYyOTkzMjM4OTk2OTY5NDIyMTMyMzU5NTY3NDI5MzAxMzgxMDE1NDA1MjExMzIyOTAyMjgyNzk3NjM0NDM3NTAxNDkiLCJ2IjoiOTI3MjIxODI0NzE5ODYxNzc0Nzk0ODU4NTgzNDAwNjY1MDM4NDQ2MjE4ODE4MDU2MDY3NjYwNzA5MTE5NDg1NTg4OTY3MDY0OTU1NTM0MjA2MDkyOTQ1MjU3NzgwMzMzMzQwMTAyNTA0MzI3OTMzNjYwNzA5NTUzMzk5MzE4OTE4NTQxNjUxMTgxMDM2NjgwOTMzNzM0MjUyNDA2NDYzMTg2OTQxODk3MDQ1NTA1OTUyMDYwNzI5NTExODM3NzA1OTM3MjQxMzYwMzcyNjM4NTEwODk1MDMxNzc5NjMxODUyNjQyOTIyNTQwNzkxOTc2NzgzMDc2OTk3MTI5MzIyNjA1NjAzNzIyMjA4ODMwOTAyNzcwNTM5Njk2ODA3MTI0MzQ0MzY0NDk1ODAzNDgxNTg4MzczMTk5Mjg3OTgyMjM2MTE5MjIwODU3OTk2NzQwNzI3MjA2OTc4NjMyMjU1NDY0NTk5NDE2NTMwOTYwOTc0ODg0NjczMDgzMzk5NjY1NDI2Mjg1NzY0NDI0NjMwMDMxODA1ODEwNjE5MDI3Njc3MjY4NDAzNzA3NzIzOTAzNDMzOTAxMzg3NDkyMzEyNzA3Nzk4MTcxMzk5ODI0NjM5NDQ1MjI4ODAxNDQ0OTIwMzk5OTkwMzAzMDYzNzg1NDg1NDU3NDIyODQwNjI1NjYyMDg5MzM3NzYxNTIzODUyMTUzMTMwMTIzMzA2NzExNjkzMTkxNTI4MTUzNzk4MjQ0ODE5OTAzMTQ4NjM0MTYzMDg2NDQ4ODkyOTc0ODgzMzQyMzUzMDI0MTA4MjM3MzY1MjI0NTI5MTA5NzEyNjE4ODc4MzY3OTc4Njg3NTY3NTM1MTEzODg1NzU4MTg0ODU2NTU1NzI3MTY0MTk0MTAyODg3MDQ1OTg4NDc2NTI0OTE2NTA0NzM5NDY1Mzk1MjQzMDc0NTY4OTU2MzExODY3OTA5ODc4NDE2MDMwODY1MjY4NDQzNTg2NzI5NjQ3NzA2NjUzNzcwNDU1NjcwOTc4NzkyNDE0Mjc5MzM1NDk0NTE1OTUxMTQ4ODg3MjAzMDg2NjYyMTEwODc1NjA5NjM4NjMxNCJ9LCJyX2NyZWRlbnRpYWwiOm51bGx9LCJzaWduYXR1cmVfY29ycmVjdG5lc3NfcHJvb2YiOnsic2UiOiIyMzQ0OTA4OTM4ODc3NDk2OTAwMTU1NzIzNTE5NzU1OTc2NDEzMzYzMDI4NzE1NTY4Nzc3MzgwNDM4NTA5NDQ0MDE0MjIzMTk5OTg4NDA1NDM0OTU3NzkzMzY5NzE0ODg3OTA4MDI1MDgwNjc0NDQ2MDQ2ODIyNDM2NTQ5Mzg1OTk3NzA5OTc3MTYwMDE1MTM0MTAzMjQ4OTkyNTI3OTYyNTk2OTIwMTA3ODUyNzYzMDUxNDUyMjM5MzEwOTQyODU1OTk3OTEwNzA0ODgxMTQ1OTE4NjI4MTU2NjA4NDUzNzc5MzcyMDIyMzI5ODM3MzYzOTQ2MDgzNTQ2OTE5NjY1OTA4MzY5NzAxMTc2NTk2Njk5MzQ1MDU2NzQ1MjI3MjY0MzczMTYyMTM4NTE0MDQxMTU3NjY5ODAzNjQ5ODgzNjg0NTA4NjQyNTc4OTM2MDc2Mjg2NDk1NzgzMTY2MTM4NjAyNjIzMDU4Nzk2OTk4NzA5NzYyODAzNzYxNzkyODI2NDUzNjIzODg4MjQ3ODkzMzc1MDQxODY0NTU0MDIwMzY5OTY0MzQwMDQ4Njg2NzAzMDM5NzkyODkzMzA1NzQ2MDE4NDkyMTIxMjk5ODI4MTYwNTU1Njg5MDQ4MzgyNTQ4OTU4NjY2OTI2MzMwMzU3OTczMDc0OTY2MzA5OTUwMDczMTMwMjA0NjgxNjI2MjQ5ODIxNTA0MjY0MTU3ODkwMzg3MTQ4OTk4MjU5NTc1MTE3NDI2MTM1MjU5MjMwMTE5MTY3NDE2MTAxNTIzMTA0NTQ1NjU4MTkyMjE2NzY5NTA5MTkxMzMxOSIsImMiOiIzNjU3Nzg5MjU2Mjk4NTIzNDUxNDYzODU4ODIxODcwNDgzOTI5MDAyNTI5NTUxMTI5ODc1NTI3NzY5NDQ2NjE5MDUyODMxMzE3ODkifSwicmV2X3JlZyI6bnVsbCwid2l0bmVzcyI6bnVsbH0"
                )
            )

        val issuedCredential = IssueCredential(
            body = IssueCredential.Body(),
            attachments = arrayOf(attachmentDescriptor),
            thid = "1",
            from = fromDID,
            to = toDID
        )

        val credential = agent.processIssuedCredentialMessage(issuedCredential)

        credential.claims.map {
            if (it.key == "emailAddress") {
                assertEquals("alice@wonderland.com", (it.value as ClaimType.StringValue).value)
            }
            if (it.key == "familyName") {
                assertEquals("Wonderland", (it.value as ClaimType.StringValue).value)
            }
            if (it.key == "dateOfIssuance") {
                assertEquals("2020-11-13T20:20:39+00:00", (it.value as ClaimType.StringValue).value)
            }
            if (it.key == "drivingLicenseID") {
                assertEquals("12345", (it.value as ClaimType.StringValue).value)
            }
            if (it.key == "drivingClass") {
                assertEquals("3", (it.value as ClaimType.StringValue).value)
            }
        }
    }

    @Test
    fun testInitiatePresentationRequest_whenJWT_thenMessageSentCorrectly() = runTest {
        val apiMock = mock<Api>()
        `when`(apiMock.request(any(), any(), any(), any(), any()))
            .thenReturn(HttpResponse(200, "Ok"))

        val apolloMock = mock<Apollo>()
        val castorMock = mock<Castor>()
        val plutoMock = mock<Pluto>()
        val mercuryMock = mock<Mercury>()
        val polluxMock = mock<Pollux>()
        val connectionManagerMock = mock<ConnectionManager>()

        val mediatorHandlerMock = mock<MediationHandler>()
        `when`(connectionManagerMock.mediationHandler).thenReturn(mediatorHandlerMock)
        val mediator = Mediator(
            id = UUID.randomUUID().toString(),
            mediatorDID = DID("did:peer:mediatordid"),
            hostDID = DID("did:peer:hostdid"),
            routingDID = DID("did:peer:routingdid")
        )
        `when`(mediatorHandlerMock.mediator).thenReturn(mediator)

        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManagerMock,
            seed = seed,
            api = apiMock,
            logger = LoggerMock()
        )

        val vmAuthentication = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("2", "1", "0")),
            controller = DID("2", "2", "0"),
            type = Curve.ED25519.value,
            publicKeyJwk = mapOf("crv" to Curve.ED25519.value, "x" to "")
        )

        val vmKeyAgreement = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("3", "1", "0")),
            controller = DID("3", "2", "0"),
            type = Curve.X25519.value,
            publicKeyJwk = mapOf("crv" to Curve.X25519.value, "x" to "")
        )

        val resolverMock = mock<DIDResolver>()
        val didDoc = DIDDocument(
            id = DID("did:prism:asdfasdf"),
            coreProperties = arrayOf(
                DIDDocument.Authentication(
                    urls = emptyArray(),
                    verificationMethods = arrayOf(vmAuthentication, vmKeyAgreement)
                )
            )
        )
        // Mock resolve did response
        `when`(castorMock.resolveDID(any())).thenReturn(didDoc)
        `when`(resolverMock.resolve(any())).thenReturn(didDoc)

        val newPeerDid = "did:peer:asdfasdf"
        // Mock createPeerDID response
        `when`(castorMock.createPeerDID(any(), any())).thenReturn(DID(newPeerDid))

        val definitionJson =
            """{"presentation_definition":{"id":"32f54163-7166-48f1-93d8-ff217bdb0653","input_descriptors":[{"id":"wa_driver_license","name":"Washington State Business License","purpose":"We can only allow licensed Washington State business representatives into the WA Business Conference","constraints":{"fields":[{"path":["${'$'}.credentialSubject.dateOfBirth","${'$'}.credentialSubject.dob","${'$'}.vc.credentialSubject.dateOfBirth","${'$'}.vc.credentialSubject.dob"]}]}}],"format":{"jwt":{"alg":["ES256K"]}}},"options":{"domain":"domain","challenge":"challenge"}}"""
        // Mock createPresentationDefinitionRequest
        `when`(polluxMock.createPresentationDefinitionRequest(any(), any(), any())).thenReturn(
            definitionJson
        )

        val toDid = "did:peer:fdsafdsa"
        val credentialType = CredentialType.JWT
        agent.initiatePresentationRequest(
            type = credentialType,
            toDID = DID(toDid),
            presentationClaims = JWTPresentationClaims(
                claims = mapOf()
            ),
            domain = "NA",
            challenge = UUID.randomUUID().toString()
        )

        val captor = argumentCaptor<Message>()
        verify(connectionManagerMock).sendMessage(captor.capture())
        val sentMessage = captor.firstValue
        assertEquals(newPeerDid, sentMessage.from.toString())
        assertEquals(toDid, sentMessage.to.toString())
        assertEquals(1, sentMessage.attachments.size)
        assertTrue(sentMessage.attachments.first().data::class == AttachmentBase64::class)
        val attachmentJsonData = sentMessage.attachments.first().data.getDataAsJsonString()
        assertEquals(definitionJson, attachmentJsonData)
    }

    @Test
    fun testInitiatePresentationRequest_whenAnoncreds_thenMessageSentCorrectly() = runTest {
        val apiMock = mock<Api>()
        `when`(apiMock.request(any(), any(), any(), any(), any()))
            .thenReturn(HttpResponse(200, "Ok"))

        val apolloMock = mock<Apollo>()
        val castorMock = mock<Castor>()
        val plutoMock = mock<Pluto>()
        val mercuryMock = mock<Mercury>()
        val polluxMock = mock<Pollux>()
        val connectionManagerMock = mock<ConnectionManager>()
        val seed = Seed(MnemonicHelper.createRandomSeed())

        val mediatorHandlerMock = mock<MediationHandler>()
        `when`(connectionManagerMock.mediationHandler).thenReturn(mediatorHandlerMock)
        val mediator = Mediator(
            id = UUID.randomUUID().toString(),
            mediatorDID = DID("did:peer:mediatordid"),
            hostDID = DID("did:peer:hostdid"),
            routingDID = DID("did:peer:routingdid")
        )
        `when`(mediatorHandlerMock.mediator).thenReturn(mediator)

        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManagerMock,
            seed = seed,
            api = apiMock,
            logger = LoggerMock()
        )

        val vmAuthentication = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("2", "1", "0")),
            controller = DID("2", "2", "0"),
            type = Curve.ED25519.value,
            publicKeyJwk = mapOf("crv" to Curve.ED25519.value, "x" to "")
        )

        val vmKeyAgreement = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("3", "1", "0")),
            controller = DID("3", "2", "0"),
            type = Curve.X25519.value,
            publicKeyJwk = mapOf("crv" to Curve.X25519.value, "x" to "")
        )

        val resolverMock = mock<DIDResolver>()
        val didDoc = DIDDocument(
            id = DID("did:prism:asdfasdf"),
            coreProperties = arrayOf(
                DIDDocument.Authentication(
                    urls = emptyArray(),
                    verificationMethods = arrayOf(vmAuthentication, vmKeyAgreement)
                )
            )
        )
        // Mock resolve did response
        `when`(castorMock.resolveDID(any())).thenReturn(didDoc)
        `when`(resolverMock.resolve(any())).thenReturn(didDoc)

        val newPeerDid = "did:peer:asdfasdf"
        // Mock createPeerDID response
        `when`(castorMock.createPeerDID(any(), any())).thenReturn(DID(newPeerDid))

        val definitionJson =
            "{\"nonce\":\"asdf1234\",\"name\":\"anoncreds_presentation_request\",\"version\":\"0.1\",\"requested_predicates\":{\"age\":{\"name\":\"age\",\"p_type\":\">=\",\"p_value\":\"18\"},\"income\":{\"name\":\"income\",\"p_type\":\"<\",\"p_value\":\"99000\"}},\"requested_attributes\":{\"name\":{\"name\":\"name\",\"restrictions\":{}}}}"
        val presentationDefinitionRequest = definitionJson
        // Mock createPresentationDefinitionRequest
        `when`(polluxMock.createPresentationDefinitionRequest(any(), any(), any())).thenReturn(
            presentationDefinitionRequest
        )

        val toDid = "did:peer:fdsafdsa"
        val credentialType = CredentialType.ANONCREDS_PROOF_REQUEST
        agent.initiatePresentationRequest(
            type = credentialType,
            toDID = DID(toDid),
            presentationClaims = AnoncredsPresentationClaims(
                predicates = mapOf(
                    "age" to AnoncredsInputFieldFilter(
                        type = "string",
                        name = "age",
                        gte = "18"
                    ),
                    "income" to AnoncredsInputFieldFilter(
                        type = "string",
                        name = "income",
                        lt = "99000"
                    )
                ),
                attributes = mapOf(
                    "name" to RequestedAttributes(
                        name = "name",
                        names = setOf(),
                        emptyMap(),
                        null
                    )
                )
            ),
            domain = "NA",
            challenge = UUID.randomUUID().toString()
        )

        val captor = argumentCaptor<Message>()
        verify(connectionManagerMock).sendMessage(captor.capture())
        val sentMessage = captor.firstValue
        assertEquals(newPeerDid, sentMessage.from.toString())
        assertEquals(toDid, sentMessage.to.toString())
        assertEquals(1, sentMessage.attachments.size)
        assertTrue(sentMessage.attachments.first().data::class == AttachmentBase64::class)
        val base64Attachment = sentMessage.attachments.first().data as AttachmentBase64
        assertEquals(definitionJson, base64Attachment.base64.base64UrlDecoded)
    }

    @Test
    fun testHandlePresentationDefinitionRequest_whenNotSupportedCredential_thenThrowInvalidCredentialError() =
        runTest {
            val apiMock = mock<Api>()
            `when`(apiMock.request(any(), any(), any(), any(), any()))
                .thenReturn(HttpResponse(200, "Ok"))

            val apolloMock = mock<Apollo>()
            val castorMock = mock<Castor>()
            val plutoMock = mock<Pluto>()
            val mercuryMock = mock<Mercury>()
            val polluxMock = mock<Pollux>()
            val connectionManagerMock = mock<ConnectionManager>()

            val mediatorHandlerMock = mock<MediationHandler>()
            `when`(connectionManagerMock.mediationHandler).thenReturn(mediatorHandlerMock)
            val mediator = Mediator(
                id = UUID.randomUUID().toString(),
                mediatorDID = DID("did:peer:mediatordid"),
                hostDID = DID("did:peer:hostdid"),
                routingDID = DID("did:peer:routingdid")
            )
            `when`(mediatorHandlerMock.mediator).thenReturn(mediator)

            val agent = EdgeAgent(
                apollo = apolloMock,
                castor = castorMock,
                pluto = plutoMock,
                mercury = mercuryMock,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = apiMock,
                logger = LoggerMock()
            )
            val msg = Json.decodeFromString<Message>(
                "{\"id\":\"00000000-685c-4004-0000-000036ac64ee\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"to\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-08T19:27:38.196506Z\",\"expiresTimePlus\":\"2024-03-09T19:27:38.196559Z\",\"attachments\":[{\"id\":\"00000000-9c2e-4249-0000-0000c1176949\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64\",\"base64\":\"eyJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjMyZjU0MTYzLTcxNjYtNDhmMS05M2Q4LWZmMjE3YmRiMDY1MyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IndhX2RyaXZlcl9saWNlbnNlIiwibmFtZSI6Ildhc2hpbmd0b24gU3RhdGUgQnVzaW5lc3MgTGljZW5zZSIsInB1cnBvc2UiOiJXZSBjYW4gb25seSBhbGxvdyBsaWNlbnNlZCBXYXNoaW5ndG9uIFN0YXRlIGJ1c2luZXNzIHJlcHJlc2VudGF0aXZlcyBpbnRvIHRoZSBXQSBCdXNpbmVzcyBDb25mZXJlbmNlIiwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiIsIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiJdfV19fV0sImZvcm1hdCI6eyJqd3QiOnsiYWxnIjpbIkVTMjU2SyJdfX19LCAib3B0aW9ucyI6IHsiZG9tYWluIjogImRvbWFpbiIsICJjaGFsbGVuZ2UiOiAiY2hhbGxlbmdlIn19\"},\"format\":\"dif/presentation-exchange/fail_test@v1.0\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
            )
            val credential = AnonCredential(
                schemaID = "",
                credentialDefinitionID = "",
                values = mapOf(),
                signatureJson = "",
                signatureCorrectnessProofJson = "",
                revocationRegistryId = null,
                revocationRegistryJson = null,
                witnessJson = "",
                json = ""
            )
            assertFailsWith(EdgeAgentError.InvalidCredentialError::class) {
                agent.preparePresentationForRequestProof(
                    RequestPresentation.fromMessage(msg),
                    credential
                )
            }
        }

    @Test
    fun testHandlePresentation_whenWrongPresentationSubmission_thenThrowMissingOrNullFieldError() =
        runTest {
            val apiMock = mock<Api>()
            `when`(apiMock.request(any(), any(), any(), any(), any()))
                .thenReturn(HttpResponse(200, "Ok"))

            val agent = EdgeAgent(
                apollo = apolloMock,
                castor = castorMock,
                pluto = plutoMock,
                mercury = mercuryMockOld,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = apiMock,
                logger = LoggerMock()
            )
            val msg = Json.decodeFromString<Message>(
                "{\"id\":\"00000000-685c-4004-0000-000036ac64ee\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"to\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-08T19:27:38.196506Z\",\"expiresTimePlus\":\"2024-03-09T19:27:38.196559Z\",\"attachments\":[{\"id\":\"00000000-9c2e-4249-0000-0000c1176949\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64\",\"base64\":\"eyJ2ZXJpZmlhYmxlUHJlc2VudGF0aW9uIjpbImV5SmhiR2NpT2lKRlV6STFOa3NpZlEuZXlKcGMzTWlPaUprYVdRNmNISnBjMjA2TWpVM01UbGhPVFppTVRVeE1qQTNNVFk1T0RGaE9EUXpNR0ZrTUdOaU9UWTRaR1ExTXpRd056TTFPVE5qT0dOa00yWXhaREkzWVRZNE1EUmxZelV3WlRwRGNHOURRM0JqUTBWc2IwdENWM1JzWlZNd2VFVkJTa05VZDI5S1l6SldhbU5FU1RGT2JYTjRSV2xCUlc5VFEyNDFkSGxFWVRaWk5uSXRTVzFUY1hCS09Ga3hiV28zU2tNelgyOVZla1V3VG5sNVJXbERRbTluYzJkT1lXVlNaR05EVWtkUWJHVTRNbFoyT1hSS1prNTNiRFp5WnpaV1kyaFNNMDl4YUdsV1lsUmhPRk5YZDI5SFdWaFdNR0ZETUhoRlFWSkRWSGR2U21NeVZtcGpSRWt4VG0xemVFVnBSRTFyUW1RMlJuUnBiMHByTTFoUFJuVXRYMk41TlZodFVpMDBkRlZSTWs1TVIybFhPR0ZKVTI5dGExSnZaelpUWkdVNVVIZHVSekJSTUZOQ1ZHMUdVMVJFWWxOTFFuWkpWalpEVkV4WWNtcEpTblIwWlVkSmJVRlRXRUZ2U0dKWFJucGtSMVo1VFVKQlFsRnJPRXREV0U1c1dUTkJlVTVVV25KTlVrbG5UemN4TUcxME1WZGZhWGhFZVZGTk0zaEpjemRVY0dwTVEwNVBSRkY0WjFab2VEVnphR1pMVGxneGIyRkpTRmRRY25jM1NWVkxiR1pwWWxGMGVEWkthelJVVTJwblkxZE9UMlpqVDNSVk9VUTVVSFZhTjFRNWRDSXNJbk4xWWlJNkltUnBaRHB3Y21semJUcGlaV1ZoTlRJek5HRm1ORFk0TURRM01UUmtPR1ZoT0dWak56ZGlOalpqWXpkbU0yVTRNVFZqTmpoaFltSTBOelZtTWpVMFkyWTVZek13TmpJMk56WXpPa056WTBKRGMxRkNSVzFSUzBReVJqRmtSMmhzWW01U2NGa3lSakJoVnpsMVRVSkJSVkZyT0V0RFdFNXNXVE5CZVU1VVduSk5Va2xuWlZObkxUSlBUekZLWkc1d2VsVlBRbWwwZWtscFkxaGtabnBsUVdOVVpsZEJUaTFaUTJWMVEySjVTV0ZKU2xFMFIxUkpNekIwWVZacGQyTm9WRE5sTUc1TVdFSlRORE5DTkdvNWFteHpiRXR2TWxwc1pGaDZha1ZzZDB0Q01qRm9Zek5TYkdOcVFWRkJWVXBRUTJkc2VscFhUbmROYWxVeVlYcEZVMGxJYTI5UWRHcHFkRk5ZV2paak1VUm5XWEpqZVVsdVJqTllPRE5uU0VVek1XZEVabTFCYm5KbmJUaHBSMmxEVlU5Q2EzbE9PVXhYYkZselNFbFZPVE4wU25reGQxVjFUbmRsU1Y5Wk5XSktVM0ZPYlZwWVZqZzBkeUlzSW01aVppSTZNVFk0TlRZek1UazVOU3dpWlhod0lqb3hOamcxTmpNMU5UazFMQ0oyWXlJNmV5SmpjbVZrWlc1MGFXRnNVM1ZpYW1WamRDSTZleUpoWkdScGRHbHZibUZzVUhKdmNESWlPaUpVWlhOME15SXNJbWxrSWpvaVpHbGtPbkJ5YVhOdE9tSmxaV0UxTWpNMFlXWTBOamd3TkRjeE5HUTRaV0U0WldNM04ySTJObU5qTjJZelpUZ3hOV00yT0dGaVlqUTNOV1l5TlRSalpqbGpNekEyTWpZM05qTTZRM05qUWtOelVVSkZiVkZMUkRKR01XUkhhR3hpYmxKd1dUSkdNR0ZYT1hWTlFrRkZVV3M0UzBOWVRteFpNMEY1VGxSYWNrMVNTV2RsVTJjdE1rOVBNVXBrYm5CNlZVOUNhWFI2U1dsaldHUm1lbVZCWTFSbVYwRk9MVmxEWlhWRFlubEpZVWxLVVRSSFZFa3pNSFJoVm1sM1kyaFVNMlV3Ymt4WVFsTTBNMEkwYWpscWJITnNTMjh5V214a1dIcHFSV3gzUzBJeU1XaGpNMUpzWTJwQlVVRlZTbEJEWjJ4NldsZE9kMDFxVlRKaGVrVlRTVWhyYjFCMGFtcDBVMWhhTm1NeFJHZFpjbU41U1c1R00xZzRNMmRJUlRNeFowUm1iVUZ1Y21kdE9HbEhhVU5WVDBKcmVVNDVURmRzV1hOSVNWVTVNM1JLZVRGM1ZYVk9kMlZKWDFrMVlrcFRjVTV0V2xoV09EUjNJbjBzSW5SNWNHVWlPbHNpVm1WeWFXWnBZV0pzWlVOeVpXUmxiblJwWVd3aVhTd2lRR052Ym5SbGVIUWlPbHNpYUhSMGNITTZYQzljTDNkM2R5NTNNeTV2Y21kY0x6SXdNVGhjTDJOeVpXUmxiblJwWVd4elhDOTJNU0pkZlgwLngwU0YxN1kwVkNEbXQ3SGNlT2RUeGZIbG9mc1ptWTE4Um42VlFiMC1yLWtfQm0zaFRpMS1rMnZrZGpCMjVoZHh5VEN2eGFtLUFrQVAtQWczQWhuNU5nIl19\"},\"format\":\"dif/presentation-exchange/definitions@v1.0\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
            )
            `when`(plutoMock.getMessageByThidAndPiuri(any(), any()))
                .thenReturn(flow { emit(null) })

            assertFailsWith(EdgeAgentError.MissingOrNullFieldError::class) {
                agent.handlePresentation(msg)
            }
        }

    @Test
    fun testHandlePresentationDefinitionRequest_whenJWT_thenSendPresentationSubmissionCorrectly() =
        runTest {
            val apiMock = mock<Api>()
            `when`(apiMock.request(any(), any(), any(), any(), any()))
                .thenReturn(HttpResponse(200, "Ok"))

            val apolloMock = mock<Apollo>()
            val castorMock = mock<Castor>()
            val plutoMock = mock<Pluto>()
            val mercuryMock = mock<Mercury>()
            val polluxMock = mock<Pollux>()
            val connectionManagerMock = mock<ConnectionManager>()

            val privateKey = Secp256k1KeyPair.generateKeyPair(
                seed = seed,
                curve = KeyCurve(Curve.SECP256K1)
            ).privateKey
            val storablePrivateKeys = listOf(
                StorablePrivateKey(
                    id = UUID.randomUUID().toString(),
                    restorationIdentifier = "secp256k1+priv",
                    data = privateKey.raw.base64UrlEncoded,
                    keyPathIndex = 0
                )
            )
            // Mock getDIDPrivateKeysByDID response
            `when`(plutoMock.getDIDPrivateKeysByDID(any())).thenReturn(flow { emit(storablePrivateKeys) })
            `when`(
                apolloMock.restorePrivateKey(
                    storablePrivateKeys.first().restorationIdentifier,
                    storablePrivateKeys.first().data
                )
            ).thenReturn(privateKey)

            val presentationSubmissionString =
                "{\"presentation_submission\":{\"id\":\"00000000-c224-45d7-0000-0000732f4932\",\"definition_id\":\"32f54163-7166-48f1-93d8-ff217bdb0653\",\"descriptor_map\":[{\"id\":\"wa_driver_license\",\"format\":\"jwt\",\"path\":\"$.verifiablePresentation[0]\"}]},\"verifiablePresentation\":[\"eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng\"]}"
            val presentationSubmission = presentationSubmissionString

            val presentationDefinitionRequest =
                """{"presentation_definition":{"id":"32f54163-7166-48f1-93d8-ff217bdb0653","input_descriptors":[{"id":"wa_driver_license","name":"Washington State Business License","purpose":"We can only allow licensed Washington State business representatives into the WA Business Conference","constraints":{"fields":[{"path":["${'$'}.credentialSubject.dateOfBirth","${'$'}.credentialSubject.dob","${'$'}.vc.credentialSubject.dateOfBirth","${'$'}.vc.credentialSubject.dob"]}]}}],"format":{"jwt":{"alg":["ES256K"]}}},"options":{"domain":"domain","challenge":"challenge"}}"""
            val credential = JWTCredential.fromJwtString(
                "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng"
            )
            // Mock createPresentationSubmission response
            `when`(polluxMock.createJWTPresentationSubmission(any(), any(), any())).thenReturn(
//            `when`(polluxMock.createJWTPresentationSubmission(presentationDefinitionRequest, credential, privateKeys.first())).thenReturn(
                presentationSubmission
            )

            val agent = EdgeAgent(
                apollo = apolloMock,
                castor = castorMock,
                pluto = plutoMock,
                mercury = mercuryMock,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = apiMock,
                logger = LoggerMock()
            )
            val msg = Json.decodeFromString<Message>(
                """{"id":"00000000-685c-4004-0000-000036ac64ee","piuri":"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation","from":{"method":"peer","methodId":"asdfasdf"},"to":{"method":"peer","methodId":"fdsafdsa"},"fromPrior":null,"body":"{}","createdTime":"2024-03-08T19:27:38.196506Z","expiresTimePlus":"2024-03-09T19:27:38.196559Z","attachments":[{"id":"00000000-9c2e-4249-0000-0000c1176949","mediaType":"application/json","data":{"type":"org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64","base64":"eyJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjMyZjU0MTYzLTcxNjYtNDhmMS05M2Q4LWZmMjE3YmRiMDY1MyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IndhX2RyaXZlcl9saWNlbnNlIiwibmFtZSI6Ildhc2hpbmd0b24gU3RhdGUgQnVzaW5lc3MgTGljZW5zZSIsInB1cnBvc2UiOiJXZSBjYW4gb25seSBhbGxvdyBsaWNlbnNlZCBXYXNoaW5ndG9uIFN0YXRlIGJ1c2luZXNzIHJlcHJlc2VudGF0aXZlcyBpbnRvIHRoZSBXQSBCdXNpbmVzcyBDb25mZXJlbmNlIiwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiIsIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiJdfV19fV0sImZvcm1hdCI6eyJqd3QiOnsiYWxnIjpbIkVTMjU2SyJdfX19LCAib3B0aW9ucyI6IHsiZG9tYWluIjogImRvbWFpbiIsICJjaGFsbGVuZ2UiOiAiY2hhbGxlbmdlIn19"},"format":"dif/presentation-exchange/definitions@v1.0"}],"thid":"00000000-ef9d-4722-0000-00003b1bc908","ack":[]}"""
            )
            val presentation = agent.preparePresentationForRequestProof(
                RequestPresentation.fromMessage(msg),
                credential
            )

            assertEquals("00000000-ef9d-4722-0000-00003b1bc908", presentation.thid)
            assertEquals("did:peer:fdsafdsa", presentation.from.toString())
            assertEquals("did:peer:asdfasdf", presentation.to.toString())
            assertEquals(1, presentation.attachments.size)
            val attachmentDescriptor = presentation.attachments.first()
            val attachmentData = attachmentDescriptor.data
            assertTrue(attachmentData is AttachmentBase64)
            val expectedPresentationSubmission =
                Json.decodeFromString<PresentationSubmission>(presentationSubmissionString)
            val attachmentDataString = attachmentData.getDataAsJsonString()
            val actualPresentationSubmission = Json.decodeFromString<PresentationSubmission>(attachmentDataString)

            assertEquals(
                expectedPresentationSubmission,
                actualPresentationSubmission
            )
        }

    @Test
    fun testPreparePresentationForRequestProof_whenJWTProof_thenSendPresentationCorrectly() =
        runTest {
            val apiMock = mock<Api>()
            `when`(apiMock.request(any(), any(), any(), any(), any()))
                .thenReturn(HttpResponse(200, "Ok"))

            val provableCredentialMock = mock<ProvableCredential>()
            val connectionManagerMock = mock<ConnectionManager>()

            val json = Json {
                ignoreUnknownKeys = true
            }
            val requestPresentationJson =
                """{"id":"581f9d51-bb0c-4bcd-a851-487a14d30cc6","piuri":"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation","from":{"method":"peer","methodId":"2.Ez6LScuoWiuQHfk4Js2aMC4Qs8rD5zNUfmiNfWMCb2pWR3FAc.Vz6MkvKtf2JqqcxhC1MPmWbWPrxqt8A4v44zri36XHgNmsmgV.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},"to":{"method":"peer","methodId":"2.Ez6LSmuL2dNc6wg5HpqDcDBXnNDG6TawcrBuxZbFkW9Hberjq.Vz6MkvcyMv5VAbTTttvNpo3YYku9Y8VMR9kRw8SFjAX8ic7JU.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},"fromPrior":"null","body":{"goal_code":"Request Proof Presentation","comment":null,"proof_types":[],"will_confirm":false},"createdTime":"1718228880","expiresTimePlus":"2024-06-13T21:48:02.636794Z","attachments":[{"id":"e784ac49-bb2d-4445-9cac-edd365664c73","data":{"type":"org.hyperledger.identus.walletsdk.domain.models.AttachmentJsonData","data":"{\"options\":{\"domain\":\"https:\\/\\/prism-verifier.com\",\"challenge\":\"11c91493-01b3-4c4d-ac36-b336bab5bddf\"},\"presentation_definition\":{\"purpose\":null,\"format\":null,\"name\":null,\"input_descriptors\":[],\"id\":\"56108c4a-ca57-40b6-89f0-1e1a2fa186fd\"}}"},"format":"prism/jwt"}],"thid":"c7cdff93-2706-4023-8d3c-3ce850ab0b2d","ack":[]}"""
            val requestPresentation = json.decodeFromString<RequestPresentation>(requestPresentationJson)

            val agent = EdgeAgent(
                apollo = apolloMock,
                castor = castorMock,
                pluto = plutoMock,
                mercury = mercuryMockOld,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = apiMock,
                logger = LoggerMock()
            )

            val credential = JWTCredential.fromJwtString(
                "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MzM3ZThmZTE0NGFhY2VkM2NhM2RkNTk0NjI0MDRmNDU5OTZlM2IyMjFhYmM0MTBhNzI1ZWE2NjUzNDg5NzJiYjpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF2eWcxYTN1cHVmbFBLczhKR1hKU3NxV1pjVG9GQXk3RjNSTFBjQlk0V25zRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFPYVBUbzM5Tnh2UmhXUW5iVWhoTXM5bTFIeEJtcV9hZWNHM0tTTGZiNWgzUkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVEZ3poOElDY1ZhNVlNZjYzRFFaM191dTNOMzNsSXVGSGJoX09KUlVIbWd2YyIsInN1YiI6ImRpZDpwcmlzbTpiZDgxZmY1NDQzNDJjMTAwNDZkZmE0YmEyOTVkNWIzNmU0Y2ZlNWE3ZWIxMjBlMTBlZTVjMjQ4NzAwNjUxMDA5OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdBIiwibmJmIjoxNzE4MjI1MDUyLCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJlbWFpbEFkZHJlc3MiOiJkZW1vQGVtYWlsLmNvbSIsImRyaXZpbmdDbGFzcyI6IjEiLCJmYW1pbHlOYW1lIjoiZGVtbyIsImRyaXZpbmdMaWNlbnNlSUQiOiJBMTIyMTMzMiIsImlkIjoiZGlkOnByaXNtOmJkODFmZjU0NDM0MmMxMDA0NmRmYTRiYTI5NWQ1YjM2ZTRjZmU1YTdlYjEyMGUxMGVlNWMyNDg3MDA2NTEwMDk6Q29VQkNvSUJFanNLQjIxaGMzUmxjakFRQVVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdCSkRDZzloZFhSb1pXNTBhV05oZEdsdmJqQVFCRW91Q2dselpXTndNalUyYXpFU0lRUHY1UDV5eWdyWndhSmxZemw1OW0ySEFES1RYVUxQc1JlMGtnZUVIdnRMZ0EiLCJkYXRlT2ZJc3N1YW5jZSI6IjAxXC8wMVwvMjAyNCJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6NCwiaWQiOiJodHRwOlwvXC8xOTIuMTY4LjY4LjExMzo4MDAwXC9wcmlzbS1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzM5YjBiNzI2LTBmNmUtNDlmNy05YzUyLTYyYTc4MTcxNzVlOCM0IiwidHlwZSI6IlN0YXR1c0xpc3QyMDIxRW50cnkiLCJzdGF0dXNMaXN0Q3JlZGVudGlhbCI6Imh0dHA6XC9cLzE5Mi4xNjguNjguMTEzOjgwMDBcL3ByaXNtLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvMzliMGI3MjYtMGY2ZS00OWY3LTljNTItNjJhNzgxNzE3NWU4In19fQ.XuMkGkJQCM5214ZjxwdHf81Jox_qyGsh011OmRtda8lTsh6TFPy9jDNey0DVijCg12qDTj-cYcFUAXe6pfvoPQ"
            )

            val pathIndexFlow = flow { emit(1) }
            `when`(plutoMock.getPrismDIDKeyPathIndex(DID(credential.subject!!))).thenReturn(pathIndexFlow)

            val testVerifiablePresentationJWTPayload = "testPayload"
            `when`(polluxMock.createJWTPresentationSubmission(any(), any(), any())).thenReturn(
                testVerifiablePresentationJWTPayload
            )

            val presentation = agent.preparePresentationForRequestProof(
                requestPresentation,
                credential
            )
            assertEquals(requestPresentation.thid, presentation.thid)
            assertEquals(requestPresentation.to.toString(), presentation.from.toString())
            assertEquals(requestPresentation.from.toString(), presentation.to.toString())
            assertEquals(1, presentation.attachments.size)
            val attachmentDescriptor = presentation.attachments.first()
            assertEquals(CredentialType.JWT.type, attachmentDescriptor.mediaType)
            val attachmentData = attachmentDescriptor.data
            assertTrue(attachmentData is AttachmentBase64)
            assertEquals(
                3,
                attachmentData.getDataAsJsonString().split(".").count()
            )
        }

    @Test
    fun testHandlePresentationSubmission_whenAttachmentNotSupported_thenThrowsAttachmentTypeNotSupported() =
        runTest {
            val apiMock = mock<Api>()
            `when`(apiMock.request(any(), any(), any(), any(), any()))
                .thenReturn(HttpResponse(200, "Ok"))

            val apolloMock = mock<Apollo>()
            val castorMock = mock<Castor>()
            val plutoMock = mock<Pluto>()
            val mercuryMock = mock<Mercury>()
            val polluxMock = mock<Pollux>()
            val connectionManagerMock = mock<ConnectionManager>()
            val seed = Seed(MnemonicHelper.createRandomSeed())

            val privateKeys = listOf(
                Secp256k1KeyPair.generateKeyPair(
                    seed = Seed(MnemonicHelper.createRandomSeed()),
                    curve = KeyCurve(Curve.SECP256K1)
                ).privateKey
            )
            val storablePrivateKeys = listOf(
                StorablePrivateKey(
                    id = UUID.randomUUID().toString(),
                    restorationIdentifier = "secp256k1+priv",
                    data = privateKeys.first().raw.base64UrlEncoded,
                    keyPathIndex = 0
                )
            )
            // Mock getDIDPrivateKeysByDID response
            `when`(plutoMock.getDIDPrivateKeysByDID(any())).thenReturn(flow { emit(storablePrivateKeys) })

            val presentationSubmission =
                """{"presentation_submission":{"id":"00000000-c224-45d7-0000-0000732f4932","definition_id":"32f54163-7166-48f1-93d8-ff217bdb0653","descriptor_map":[{"id":"wa_driver_license","format":"jwt","path":"${'$'}.verifiablePresentation[0]"}]},"verifiablePresentation":["eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng"]}"""
            // Mock createPresentationSubmission response
            `when`(polluxMock.createJWTPresentationSubmission(any(), any(), any())).thenReturn(
                presentationSubmission
            )

            val vmAuthentication = DIDDocument.VerificationMethod(
                id = DIDUrl(DID("2", "1", "0")),
                controller = DID("2", "2", "0"),
                type = Curve.ED25519.value,
                publicKeyJwk = mapOf("crv" to Curve.ED25519.value, "x" to "")
            )

            val vmKeyAgreement = DIDDocument.VerificationMethod(
                id = DIDUrl(DID("3", "1", "0")),
                controller = DID("3", "2", "0"),
                type = Curve.X25519.value,
                publicKeyJwk = mapOf("crv" to Curve.X25519.value, "x" to "")
            )

            val resolverMock = mock<DIDResolver>()
            val didDoc = DIDDocument(
                id = DID("did:prism:asdfasdf"),
                coreProperties = arrayOf(
                    DIDDocument.Authentication(
                        urls = emptyArray(),
                        verificationMethods = arrayOf(vmAuthentication, vmKeyAgreement)
                    )
                )
            )
            // Mock resolve did response
            `when`(castorMock.resolveDID(any())).thenReturn(didDoc)
            `when`(resolverMock.resolve(any())).thenReturn(didDoc)

            val agent = EdgeAgent(
                apollo = apolloMock,
                castor = castorMock,
                pluto = plutoMock,
                mercury = mercuryMock,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = apiMock,
                logger = LoggerMock()
            )

            val msgString =
                "{\"id\":\"00000000-621a-4ae9-0000-00002ffb05bf\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"to\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-18T17:11:58.053680Z\",\"expiresTimePlus\":\"2024-03-19T17:11:58.058523Z\",\"attachments\":[{\"id\":\"00000000-ef5f-40c0-0000-0000d2674b80\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentJsonData\",\"data\":\"eyJwcmVzZW50YXRpb25fc3VibWlzc2lvbiI6eyJpZCI6IjAwMDAwMDAwLWMyMjQtNDVkNy0wMDAwLTAwMDA3MzJmNDkzMiIsImRlZmluaXRpb25faWQiOiIzMmY1NDE2My03MTY2LTQ4ZjEtOTNkOC1mZjIxN2JkYjA2NTMiLCJkZXNjcmlwdG9yX21hcCI6W3siaWQiOiJ3YV9kcml2ZXJfbGljZW5zZSIsImZvcm1hdCI6Imp3dF92cCIsInBhdGgiOiIkLnZlcmlmaWFibGVDcmVkZW50aWFsWzBdIn1dfSwidmVyaWZpYWJsZUNyZWRlbnRpYWwiOlt7InZjIjp7ImNvbnRleHQiOltdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImFkZGl0aW9uYWxQcm9wMiI6IlRlc3QzIiwiaWQiOiJkaWQ6cHJpc206YmVlYTUyMzRhZjQ2ODA0NzE0ZDhlYThlYzc3YjY2Y2M3ZjNlODE1YzY4YWJiNDc1ZjI1NGNmOWMzMDYyNjc2MzpDc2NCQ3NRQkVtUUtEMkYxZEdobGJuUnBZMkYwYVc5dU1CQUVRazhLQ1hObFkzQXlOVFpyTVJJZ2VTZy0yT08xSmRucHpVT0JpdHpJaWNYZGZ6ZUFjVGZXQU4tWUNldUNieUlhSUpRNEdUSTMwdGFWaXdjaFQzZTBuTFhCUzQzQjRqOWpsc2xLbzJabGRYempFbHdLQjIxaGMzUmxjakFRQVVKUENnbHpaV053TWpVMmF6RVNJSGtvUHRqanRTWFo2YzFEZ1lyY3lJbkYzWDgzZ0hFMzFnRGZtQW5yZ204aUdpQ1VPQmt5TjlMV2xZc0hJVTkzdEp5MXdVdU53ZUlfWTViSlNxTm1aWFY4NHcifX19XSwicHJvb2YiOnsidHlwZSI6IkVjZHNhU2VjcDI1NmsxU2lnbmF0dXJlMjAxOSIsImNyZWF0ZWQiOiIyOCBKdW5lIDU2MTU1LCAwNzozMToxMCIsInByb29mUHVycG9zZSI6ImF1dGhlbnRpY2F0aW9uIiwidmVyaWZpY2F0aW9uTWV0aG9kIjoiZGlkOnByaXNtOmFzZGZhc2RmYXNkZmFzZGYja2V5cy0xIiwiandzIjoiZXlKaGJHY2lPaUpGVXpJMU5rc2lmUS5leUpwYzNNaU9pSmthV1E2Y0hKcGMyMDZNalUzTVRsaE9UWmlNVFV4TWpBM01UWTVPREZoT0RRek1HRmtNR05pT1RZNFpHUTFNelF3TnpNMU9UTmpPR05rTTJZeFpESTNZVFk0TURSbFl6VXdaVHBEY0c5RFEzQmpRMFZzYjB0Q1YzUnNaVk13ZUVWQlNrTlVkMjlLWXpKV2FtTkVTVEZPYlhONFJXbEJSVzlUUTI0MWRIbEVZVFpaTm5JdFNXMVRjWEJLT0ZreGJXbzNTa016WDI5VmVrVXdUbmw1UldsRFFtOW5jMmRPWVdWU1pHTkRVa2RRYkdVNE1sWjJPWFJLWms1M2JEWnlaelpXWTJoU00wOXhhR2xXWWxSaE9GTlhkMjlIV1ZoV01HRkRNSGhGUVZKRFZIZHZTbU15Vm1walJFa3hUbTF6ZUVWcFJFMXJRbVEyUm5ScGIwcHJNMWhQUm5VdFgyTjVOVmh0VWkwMGRGVlJNazVNUjJsWE9HRkpVMjl0YTFKdlp6WlRaR1U1VUhkdVJ6QlJNRk5DVkcxR1UxUkVZbE5MUW5aSlZqWkRWRXhZY21wSlNuUjBaVWRKYlVGVFdFRnZTR0pYUm5wa1IxWjVUVUpCUWxGck9FdERXRTVzV1ROQmVVNVVXbkpOVWtsblR6Y3hNRzEwTVZkZmFYaEVlVkZOTTNoSmN6ZFVjR3BNUTA1UFJGRjRaMVpvZURWemFHWkxUbGd4YjJGSlNGZFFjbmMzU1ZWTGJHWnBZbEYwZURaS2F6UlVVMnBuWTFkT1QyWmpUM1JWT1VRNVVIVmFOMVE1ZENJc0luTjFZaUk2SW1ScFpEcHdjbWx6YlRwaVpXVmhOVEl6TkdGbU5EWTRNRFEzTVRSa09HVmhPR1ZqTnpkaU5qWmpZemRtTTJVNE1UVmpOamhoWW1JME56Vm1NalUwWTJZNVl6TXdOakkyTnpZek9rTnpZMEpEYzFGQ1JXMVJTMFF5UmpGa1IyaHNZbTVTY0ZreVJqQmhWemwxVFVKQlJWRnJPRXREV0U1c1dUTkJlVTVVV25KTlVrbG5aVk5uTFRKUFR6RktaRzV3ZWxWUFFtbDBla2xwWTFoa1pucGxRV05VWmxkQlRpMVpRMlYxUTJKNVNXRkpTbEUwUjFSSk16QjBZVlpwZDJOb1ZETmxNRzVNV0VKVE5ETkNOR281YW14emJFdHZNbHBzWkZoNmFrVnNkMHRDTWpGb1l6TlNiR05xUVZGQlZVcFFRMmRzZWxwWFRuZE5hbFV5WVhwRlUwbElhMjlRZEdwcWRGTllXalpqTVVSbldYSmplVWx1UmpOWU9ETm5TRVV6TVdkRVptMUJibkpuYlRocFIybERWVTlDYTNsT09VeFhiRmx6U0VsVk9UTjBTbmt4ZDFWMVRuZGxTVjlaTldKS1UzRk9iVnBZVmpnMGR5SXNJbTVpWmlJNk1UWTROVFl6TVRrNU5Td2laWGh3SWpveE5qZzFOak0xTlRrMUxDSjJZeUk2ZXlKamNtVmtaVzUwYVdGc1UzVmlhbVZqZENJNmV5SmhaR1JwZEdsdmJtRnNVSEp2Y0RJaU9pSlVaWE4wTXlJc0ltbGtJam9pWkdsa09uQnlhWE50T21KbFpXRTFNak0wWVdZME5qZ3dORGN4TkdRNFpXRTRaV00zTjJJMk5tTmpOMll6WlRneE5XTTJPR0ZpWWpRM05XWXlOVFJqWmpsak16QTJNalkzTmpNNlEzTmpRa056VVVKRmJWRkxSREpHTVdSSGFHeGlibEp3V1RKR01HRlhPWFZOUWtGRlVXczRTME5ZVG14Wk0wRjVUbFJhY2sxU1NXZGxVMmN0TWs5UE1VcGtibkI2VlU5Q2FYUjZTV2xqV0dSbWVtVkJZMVJtVjBGT0xWbERaWFZEWW5sSllVbEtVVFJIVkVrek1IUmhWbWwzWTJoVU0yVXdia3hZUWxNME0wSTBhamxxYkhOc1MyOHlXbXhrV0hwcVJXeDNTMEl5TVdoak0xSnNZMnBCVVVGVlNsQkRaMng2V2xkT2QwMXFWVEpoZWtWVFNVaHJiMUIwYW1wMFUxaGFObU14UkdkWmNtTjVTVzVHTTFnNE0yZElSVE14WjBSbWJVRnVjbWR0T0dsSGFVTlZUMEpyZVU0NVRGZHNXWE5JU1ZVNU0zUktlVEYzVlhWT2QyVkpYMWsxWWtwVGNVNXRXbGhXT0RSM0luMHNJblI1Y0dVaU9sc2lWbVZ5YVdacFlXSnNaVU55WldSbGJuUnBZV3dpWFN3aVFHTnZiblJsZUhRaU9sc2lhSFIwY0hNNlhDOWNMM2QzZHk1M015NXZjbWRjTHpJd01UaGNMMk55WldSbGJuUnBZV3h6WEM5Mk1TSmRmWDAueDBTRjE3WTBWQ0RtdDdIY2VPZFR4Zkhsb2ZzWm1ZMThSbjZWUWIwLXIta19CbTNoVGkxLWsydmtkakIyNWhkeHlUQ3Z4YW0tQWtBUC1BZzNBaG41TmciLCJjaGFsbGVuZ2UiOiIzMDQ1MDIyMTAwYjE0MTJjMGYzZmJiYzVjODc2ZGRlNjExNDFmYTY4N2Y3ZjJmYWJhODM0YWJjZTA5Yzg2YzcwNWEwYjkwMjAwNTAyMjA2YjY3MjUzZmE1ZjgwMzQ0YzQyZGQ4NGQyMzZiYmJiMTVkNTBhODliODE2ZmE1NWQ1YTZhNzQyY2NjODYwZTIzIn19\"},\"format\":\"prism/jwt\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
            val msg = Json.decodeFromString<Message>(msgString)

            assertFailsWith<NullPointerException> {
                agent.handlePresentation(msg)
            }
        }

    @Test
    fun testHandlePresentationSubmission_whenJWT_thenReturnTrue() = runTest {
        val apiMock = mock<Api>()
        `when`(apiMock.request(any(), any(), any(), any(), any()))
            .thenReturn(HttpResponse(200, "Ok"))

        val apolloMock = mock<Apollo>()
        val castorMock = mock<Castor>()
        val plutoMock = mock<Pluto>()
        val mercuryMock = mock<Mercury>()
        val polluxMock = mock<Pollux>()
        val connectionManagerMock = mock<ConnectionManager>()

        val privateKey = Secp256k1KeyPair.generateKeyPair(
            seed = seed,
            curve = KeyCurve(Curve.SECP256K1)
        ).privateKey
        val storablePrivateKeys = listOf(
            StorablePrivateKey(
                id = UUID.randomUUID().toString(),
                restorationIdentifier = "secp256k1+priv",
                data = privateKey.raw.base64UrlEncoded,
                keyPathIndex = 0
            )
        )
        // Mock getDIDPrivateKeysByDID response
        `when`(plutoMock.getDIDPrivateKeysByDID(any())).thenReturn(flow { emit(storablePrivateKeys) })
        `when`(
            apolloMock.restorePrivateKey(
                storablePrivateKeys.first().restorationIdentifier,
                storablePrivateKeys.first().data
            )
        ).thenReturn(privateKey)

        val requestMsg = Json.decodeFromString<Message>(
            "{\"id\":\"00000000-685c-4004-0000-000036ac64ee\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"to\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-08T19:27:38.196506Z\",\"expiresTimePlus\":\"2024-03-09T19:27:38.196559Z\",\"attachments\":[{\"id\":\"00000000-9c2e-4249-0000-0000c1176949\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64\",\"base64\":\"eyJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjMyZjU0MTYzLTcxNjYtNDhmMS05M2Q4LWZmMjE3YmRiMDY1MyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IndhX2RyaXZlcl9saWNlbnNlIiwibmFtZSI6Ildhc2hpbmd0b24gU3RhdGUgQnVzaW5lc3MgTGljZW5zZSIsInB1cnBvc2UiOiJXZSBjYW4gb25seSBhbGxvdyBsaWNlbnNlZCBXYXNoaW5ndG9uIFN0YXRlIGJ1c2luZXNzIHJlcHJlc2VudGF0aXZlcyBpbnRvIHRoZSBXQSBCdXNpbmVzcyBDb25mZXJlbmNlIiwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiIsIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiJdfV19fV0sImZvcm1hdCI6eyJqd3QiOnsiYWxnIjpbIkVTMjU2SyJdfX19LCAib3B0aW9ucyI6IHsiZG9tYWluIjogImRvbWFpbiIsICJjaGFsbGVuZ2UiOiAiY2hhbGxlbmdlIn19\"},\"format\":\"dif/presentation-exchange/definitions@v1.0\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
        )
        `when`(
            plutoMock.getMessageByThidAndPiuri(
                any(),
                any()
            )
        ).thenReturn(flow { emit(requestMsg) })

        val presentationSubmission =
            "{\"presentation_submission\":{\"id\":\"015a6303-3a16-4813-a657-54a12ff5dab4\",\"definition_id\":\"32f54163-7166-48f1-93d8-ff217bdb0653\",\"descriptor_map\":[{\"id\":\"wa_driver_license\",\"format\":\"jwt\",\"path\":\"$.verifiablePresentation[0]\"}]},\"verifiablePresentation\":[\"eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng\"]}"

        // Mock createPresentationSubmission response
        `when`(polluxMock.createJWTPresentationSubmission(any(), any(), any())).thenReturn(
            presentationSubmission
        )
        `when`(polluxMock.verifyPresentationSubmission(any(), any())).thenReturn(true)

        val mockEcPublicKey = mock<ECPublicKey>()
        `when`(polluxMock.extractEcPublicKeyFromVerificationMethod(any())).thenReturn(
            arrayOf(
                mockEcPublicKey
            )
        )

        val vmAuthentication = DIDDocument.VerificationMethod(
            id = DIDUrl(
                DID(
                    "did",
                    "prism",
                    "607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374"
                ),
                fragment = "auth-1"
            ),
            controller = DID(
                "did",
                "prism",
                "607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374"
            ),
            type = Curve.ED25519.value,
            publicKeyJwk = mapOf(
                "crv" to Curve.ED25519.value,
                "x" to "4r3o5WTLuNKmcLW6pOL_32QIoWja6BdI0lf4nJSCRzM",
                "y" to "Ftj13x29r6oi8p7iy_4dY4u1U50PVFPnLuSputu3r9A",
                "kty" to "EC"
            )
        )

        val authenticationDidUrls = arrayOf(
            "did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374#auth-1"
        )
        val authentication = DIDDocument.AssertionMethod(
            urls = authenticationDidUrls,
            verificationMethods = arrayOf(vmAuthentication)
        )

        val resolverMock = mock<DIDResolver>()
        val didDoc = DIDDocument(
            id = DID("did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374"),
            coreProperties = arrayOf(authentication)
        )

        // Mock resolve did response
        `when`(castorMock.resolveDID(any())).thenReturn(didDoc)
        `when`(resolverMock.resolve(any())).thenReturn(didDoc)
        `when`(castorMock.verifySignature(any(), any(), any())).thenReturn(true)

        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManagerMock,
            seed = seed,
            api = apiMock,
            logger = LoggerMock()
        )

        val msgString =
            "{\"id\":\"00000000-621a-4ae9-0000-00002ffb05bf\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"to\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-18T17:11:58.053680Z\",\"expiresTimePlus\":\"2024-03-19T17:11:58.058523Z\",\"attachments\":[{\"id\":\"00000000-ef5f-40c0-0000-0000d2674b80\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64\",\"base64\":\"eyJwcmVzZW50YXRpb25fc3VibWlzc2lvbiI6eyJpZCI6IjAwMDAwMDAwLWMyMjQtNDVkNy0wMDAwLTAwMDA3MzJmNDkzMiIsImRlZmluaXRpb25faWQiOiIzMmY1NDE2My03MTY2LTQ4ZjEtOTNkOC1mZjIxN2JkYjA2NTMiLCJkZXNjcmlwdG9yX21hcCI6W3siaWQiOiJ3YV9kcml2ZXJfbGljZW5zZSIsImZvcm1hdCI6Imp3dCIsInBhdGgiOiIkLnZlcmlmaWFibGVQcmVzZW50YXRpb25bMF0ifV19LCJ2ZXJpZmlhYmxlUHJlc2VudGF0aW9uIjpbImV5SmhiR2NpT2lKRlV6STFOa3NpZlEuZXlKcGMzTWlPaUprYVdRNmNISnBjMjA2TWpVM01UbGhPVFppTVRVeE1qQTNNVFk1T0RGaE9EUXpNR0ZrTUdOaU9UWTRaR1ExTXpRd056TTFPVE5qT0dOa00yWXhaREkzWVRZNE1EUmxZelV3WlRwRGNHOURRM0JqUTBWc2IwdENWM1JzWlZNd2VFVkJTa05VZDI5S1l6SldhbU5FU1RGT2JYTjRSV2xCUlc5VFEyNDFkSGxFWVRaWk5uSXRTVzFUY1hCS09Ga3hiV28zU2tNelgyOVZla1V3VG5sNVJXbERRbTluYzJkT1lXVlNaR05EVWtkUWJHVTRNbFoyT1hSS1prNTNiRFp5WnpaV1kyaFNNMDl4YUdsV1lsUmhPRk5YZDI5SFdWaFdNR0ZETUhoRlFWSkRWSGR2U21NeVZtcGpSRWt4VG0xemVFVnBSRTFyUW1RMlJuUnBiMHByTTFoUFJuVXRYMk41TlZodFVpMDBkRlZSTWs1TVIybFhPR0ZKVTI5dGExSnZaelpUWkdVNVVIZHVSekJSTUZOQ1ZHMUdVMVJFWWxOTFFuWkpWalpEVkV4WWNtcEpTblIwWlVkSmJVRlRXRUZ2U0dKWFJucGtSMVo1VFVKQlFsRnJPRXREV0U1c1dUTkJlVTVVV25KTlVrbG5UemN4TUcxME1WZGZhWGhFZVZGTk0zaEpjemRVY0dwTVEwNVBSRkY0WjFab2VEVnphR1pMVGxneGIyRkpTRmRRY25jM1NWVkxiR1pwWWxGMGVEWkthelJVVTJwblkxZE9UMlpqVDNSVk9VUTVVSFZhTjFRNWRDSXNJbk4xWWlJNkltUnBaRHB3Y21semJUcGlaV1ZoTlRJek5HRm1ORFk0TURRM01UUmtPR1ZoT0dWak56ZGlOalpqWXpkbU0yVTRNVFZqTmpoaFltSTBOelZtTWpVMFkyWTVZek13TmpJMk56WXpPa056WTBKRGMxRkNSVzFSUzBReVJqRmtSMmhzWW01U2NGa3lSakJoVnpsMVRVSkJSVkZyT0V0RFdFNXNXVE5CZVU1VVduSk5Va2xuWlZObkxUSlBUekZLWkc1d2VsVlBRbWwwZWtscFkxaGtabnBsUVdOVVpsZEJUaTFaUTJWMVEySjVTV0ZKU2xFMFIxUkpNekIwWVZacGQyTm9WRE5sTUc1TVdFSlRORE5DTkdvNWFteHpiRXR2TWxwc1pGaDZha1ZzZDB0Q01qRm9Zek5TYkdOcVFWRkJWVXBRUTJkc2VscFhUbmROYWxVeVlYcEZVMGxJYTI5UWRHcHFkRk5ZV2paak1VUm5XWEpqZVVsdVJqTllPRE5uU0VVek1XZEVabTFCYm5KbmJUaHBSMmxEVlU5Q2EzbE9PVXhYYkZselNFbFZPVE4wU25reGQxVjFUbmRsU1Y5Wk5XSktVM0ZPYlZwWVZqZzBkeUlzSW01aVppSTZNVFk0TlRZek1UazVOU3dpWlhod0lqb3hOamcxTmpNMU5UazFMQ0oyWXlJNmV5SmpjbVZrWlc1MGFXRnNVM1ZpYW1WamRDSTZleUpoWkdScGRHbHZibUZzVUhKdmNESWlPaUpVWlhOME15SXNJbWxrSWpvaVpHbGtPbkJ5YVhOdE9tSmxaV0UxTWpNMFlXWTBOamd3TkRjeE5HUTRaV0U0WldNM04ySTJObU5qTjJZelpUZ3hOV00yT0dGaVlqUTNOV1l5TlRSalpqbGpNekEyTWpZM05qTTZRM05qUWtOelVVSkZiVkZMUkRKR01XUkhhR3hpYmxKd1dUSkdNR0ZYT1hWTlFrRkZVV3M0UzBOWVRteFpNMEY1VGxSYWNrMVNTV2RsVTJjdE1rOVBNVXBrYm5CNlZVOUNhWFI2U1dsaldHUm1lbVZCWTFSbVYwRk9MVmxEWlhWRFlubEpZVWxLVVRSSFZFa3pNSFJoVm1sM1kyaFVNMlV3Ymt4WVFsTTBNMEkwYWpscWJITnNTMjh5V214a1dIcHFSV3gzUzBJeU1XaGpNMUpzWTJwQlVVRlZTbEJEWjJ4NldsZE9kMDFxVlRKaGVrVlRTVWhyYjFCMGFtcDBVMWhhTm1NeFJHZFpjbU41U1c1R00xZzRNMmRJUlRNeFowUm1iVUZ1Y21kdE9HbEhhVU5WVDBKcmVVNDVURmRzV1hOSVNWVTVNM1JLZVRGM1ZYVk9kMlZKWDFrMVlrcFRjVTV0V2xoV09EUjNJbjBzSW5SNWNHVWlPbHNpVm1WeWFXWnBZV0pzWlVOeVpXUmxiblJwWVd3aVhTd2lRR052Ym5SbGVIUWlPbHNpYUhSMGNITTZYQzljTDNkM2R5NTNNeTV2Y21kY0x6SXdNVGhjTDJOeVpXUmxiblJwWVd4elhDOTJNU0pkZlgwLngwU0YxN1kwVkNEbXQ3SGNlT2RUeGZIbG9mc1ptWTE4Um42VlFiMC1yLWtfQm0zaFRpMS1rMnZrZGpCMjVoZHh5VEN2eGFtLUFrQVAtQWczQWhuNU5nIl19\"},\"format\":\"prism/jwt\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
        val msg = Json.decodeFromString<Message>(msgString)

        assertTrue(agent.handlePresentation(msg))
    }

    @Test
    fun `EdgeAgent backup wallet should generate a JWE with the proper algorithm and encryption method`() = runTest {
        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManagerMock,
            seed = seed,
            api = null,
            logger = LoggerMock()
        )
        `when`(
            apolloMock.createPrivateKey(
                mapOf(
                    TypeKey().property to KeyTypes.Curve25519,
                    CurveKey().property to Curve.X25519.value,
                    SeedKey().property to seed.value.base64UrlEncoded,
                    DerivationPathKey().property to "m/0'/0'/0'"
                )
            )
        ).thenReturn(
            X25519PrivateKey(
                byteArrayOf(
                    32,
                    28,
                    -51,
                    70,
                    102,
                    -26,
                    -24,
                    -94,
                    -14,
                    -51,
                    -68,
                    -39,
                    -61,
                    -51,
                    21,
                    -60,
                    94,
                    -51,
                    115,
                    -79,
                    72,
                    77,
                    -39,
                    45,
                    126,
                    -26,
                    27,
                    -10,
                    35,
                    25,
                    -48,
                    90
                )
            )
        )

        `when`(plutoMock.getAllDIDs()).thenReturn(flow { emit(Json.decodeFromString<List<DID>>(getDids)) })
        `when`(plutoMock.getAllCredentials()).thenReturn(
            flow {
                @Serializable
                data class CredentialMock(
                    val restorationId: String,
                    val credentialData: String,
                    val revoked: Boolean
                )

                fun String.toRestorationId(): RestorationID =
                    RestorationID.entries.first {
                        it.value == this
                    }

                val credentials = Json.decodeFromString<List<CredentialMock>>(getCredentials).map {
                    val currentCredential = when (it.restorationId.toRestorationId()) {
                        RestorationID.JWT -> {
                            val jwtString = it.credentialData.base64UrlDecoded
                            JWTCredential.fromJwtString(jwtString).toStorableCredential()
                        }

                        RestorationID.ANONCRED -> {
                            val data = it.credentialData.base64UrlDecodedBytes
                            PlutoRestoreTask.AnonCredentialBackUp.fromStorableData(data)
                                .toAnonCredential().toStorableCredential()
                        }

                        RestorationID.W3C -> {
                            throw Exception("This should never happen in this test class")
                        }
                    }
                    CredentialRecovery(
                        restorationId = it.restorationId,
                        credentialData = currentCredential.credentialData,
                        revoked = it.revoked
                    )
                }
                emit(
                    credentials
                )
            }
        )
        `when`(plutoMock.getAllDidPairs()).thenReturn(
            flow {
                emit(
                    Json.decodeFromString<List<DIDPair>>(
                        getDidPairs
                    )
                )
            }
        )
        `when`(plutoMock.getAllKeysForBackUp()).thenReturn(
            flow {
                emit(
                    Json.decodeFromString<List<BackupV0_0_1.Key>>(
                        getPrivateKeys
                    )
                )
            }
        )
        `when`(plutoMock.getLinkSecret()).thenReturn(
            flow {
                emit(
                    Json.decodeFromString<String?>(
                        getLinkSecret
                    )
                )
            }
        )
        `when`(plutoMock.getAllMessages()).thenReturn(
            flow {
                emit(
                    Json.decodeFromString<List<Message>>(
                        getMessages
                    )
                )
            }
        )
        `when`(plutoMock.getAllMediators()).thenReturn(
            flow {
                emit(
                    Json.decodeFromString<List<Mediator>>(
                        getMediator
                    )
                )
            }
        )

        val jwe = agent.backupWallet(plutoBackupTask = PlutoBackupTask(plutoMock))
        val jweObject = JWEObject.parse(jwe)
        assertEquals(JWEAlgorithm.ECDH_ES_A256KW, jweObject.header.algorithm)
        assertEquals(EncryptionMethod.A256CBC_HS512, jweObject.header.encryptionMethod)
    }

    @Test
    fun `test AttachementData getDataAsString`() = runTest {
        val message = Json.decodeFromString<Message>(
            """{"id":"e430e4af-455e-4a15-9f2f-5bd8e5f350b8","piuri":"https://didcomm.org/issue-credential/3.0/offer-credential","from":{"method":"peer","methodId":"2.Ez6LSm5hETc4CS4X8RxYYKjoS2B3CM8TyzbgRrE7kGrdymHdq.Vz6MkoP2VXs4N7iNsKTzEKtZbnfu6yDH1x2ajGtCmNmc6qdMW.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},"to":{"method":"peer","methodId":"2.Ez6LSd8irQSWjjMfvg53kcaDY6Q2doPEvQwscjSzidgWoFUVK.Vz6Mksu4QVe8oKwJEDPgxRg2bFa3QWrZR1EZGC9xq8xk9twYX.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},"fromPrior":"null","body":"{\"multiple_available\":null,\"goal_code\":\"Offer Credential\",\"credential_preview\":{\"schema_id\":\"http:\\/\\/192.168.68.113:8000\\/cloud-agent\\/schema-registry\\/schemas\\/5667190d-640c-36af-a9f1-f4ed2587e766\\/schema\",\"type\":\"https:\\/\\/didcomm.org\\/issue-credential\\/3.0\\/credential-credential\",\"body\":{\"attributes\":[{\"media_type\":null,\"name\":\"age\",\"value\":\"18\"},{\"media_type\":null,\"name\":\"name\",\"value\":\"Cristian\"}]}},\"replacement_id\":null,\"comment\":null}","created_time":"1721242264","expires_time_plus":"1721328667","attachments":[{"id":"ee903fe0-2c49-4356-9b41-cfccc979c0a1","data":{"base64":"eyJzY2hlbWFfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9jbG91ZC1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy81NjY3MTkwZC02NDBjLTM2YWYtYTlmMS1mNGVkMjU4N2U3NjYvc2NoZW1hIiwiY3JlZF9kZWZfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9jbG91ZC1hZ2VudC9jcmVkZW50aWFsLWRlZmluaXRpb24tcmVnaXN0cnkvZGVmaW5pdGlvbnMvM2RhNmExZDgtMmIyMy0zMTM4LWIwMmEtYWIwYmI0OGY4MGY5L2RlZmluaXRpb24iLCJrZXlfY29ycmVjdG5lc3NfcHJvb2YiOnsiYyI6IjM0MjE0MzM4OTA5MDk4MTU5ODIyNTA3MjQ4Njk3NjUyNTIyMzc1NzM2ODM0OTM1MDg0NjM5MTYzNTUyNjgwMjc3MjQyOTcxODI5ODUwIiwieHpfY2FwIjoiODAwNzA3NDcxNzUyOTQ3MTI1NDIxNTU2ODI1ODYyNDc5NzE1OTE0OTE3ODE4NTY4MDI1NjU5Mzk1NDcyMTAzMzQ0NjAwMDI4NzU4MzczMTA5Mjg0NzEzNDg5MDg1OTk3NzE5NDcwOTc5MDQ1NzA3NTY1ODA4MDczNzYxMjI0OTI3MzcyMDk1MTU2MzAxODE3MzgzNDE5NzE4NzM0NDk2MDQwMjc1MzM2MTg0ODIzNzQ4NTg0NzgzNjIxNzE1NTQwOTI3Nzk0ODQyMTA2NDM1NDEwNzc4MDg2ODg1MTc5MzQzMjEzMjU3NTk2ODM2NjU5NzYwMTI4NzI5ODI5MTk2MzI0MjQwMzgyOTc3MzczNjU3MTA0NjQ5NjE4MjU0MDMzMDk4Njc0OTkxMzIwMzc2NTEyMTUyOTk3Mzg0Mzk5MzY0OTc3MDM4NzU2ODcwOTU3NTcyMDM0NTM0NTY1MDM5OTY0MTYzNDgzNDEyMTEzNzc0NzA5MTU2NTcxMjcxNTI3ODY2NzQwNzU1Nzc2MzIzOTgxNTE5NzEzNjQ1MjQ4Njc5NzgyOTM3NTcyOTI4NDI1NzQ0NjE3MjMwNzk2MzYwNzE5MDA3Mzc0ODgwNTI2ODA4ODIzODg2NTMwMzE2MzgyOTI1MDcxOTYzMjUwMzgyNzU4MDA5Mjk1MzI1NDgzODIyNTg0OTIyMDkwMTcwMTAxOTY5OTk5MTg1MzAxMDA1NDM2MjYyMDI4NDIxOTA0NjU5MzY4OTU3OTQyNTAwNDkxNzI5MjY0MTMzMzE5Nzg0NDIwNjQ0NDUxMTUyNjA2MDI2MDg1MDkwNDYzNzAzMTQzNDczMTcxMjg3OTUxMTM0NTM1MDY0NDc0NjQ5NDE5MzUzMDQ4MzQwOTk4MTY2NjYwNDkyMDE5ODQyNDU2NDM5MTcxNjM2IiwieHJfY2FwIjpbWyJhZ2UiLCI3MjA2NjI4MDAzNzAwMjM5MzM5NjAzNjUxNDQ1MTY2NDE0MjcwNTA0ODM4ODAwMzYxMjE0NjQ3MjA1ODYwOTczMTg4MTQ0OTE2OTkwOTE3NTIyNjU4Mzg3Njg1MTcwMzkyMDcyMjk0MTUxMDUwODk1Njc0MTU0OTYwMDMyMDM0Mzg2MzEzMzY0MTMwMDQwNzI5ODQwMjcxMzcyNjc4NTI0NjE0NjUxNDcxNDU0NTg4MzgwMzI2OTMwODQ0Njc4Nzg3MzA3NzY3ODk2NjY5NTE2MDY1Njc3MDA3MjQyMDEwMjQyMDAwNTg4NjgxMTczNDUxNjg4NTU5MDEwMjQyNTgyMTg4MTY5MTUyNjUwNzY4NzgxNjMyMjgwNTgyODI1NjM3MjY0NzUwMTA3NTU2NDQzNTgyNzMwNDIxMjE3NTI4OTgyNTE5MzA3NzQ0ODAxNTYyNTYyMzQzNTcyNzU4NDEzNjc1NzY0ODQwOTY5MTY3NTE3ODcyNjk2MDY1MDM2MDU1MzgwMDg2NjcyNjUzMDEyMTIxMDk2MTA5OTQyMTg1NjM2ODk3MDE3Mjc5NDg3NjEyNDczNzc4NDUxMjkxMjE3NDg3ODQxOTc1NjI2MDczMjI0ODQ1MjI4NDM1OTk0MjI2MTg1MDc1NDI4MjA3OTg5MzAwMjExMzI5OTM4NjQxMzEwMTk5MjcwNTE0NjA2ODU5NDEzNDY1NzE1MjQyNjk0ODc0ODkwNDAzNDk5MzUxOTIxMDY4OTMwODE1ODY5ODM5NDYyMTE0MDI2MjM3MzY5OTAwMzE2MTA0NzYwMDAwNzk2NjcxOTUzNTAxMjcxMTI3MjM4NzM5NDI0Mjc2ODQyODkwNjQwNDY3NjYxNDEzODQ5Mzc5NzEwNzcxNjg0NzU4NTY1NzY2MDY4NzgwNjY0NjI3MjgiXSxbIm1hc3Rlcl9zZWNyZXQiLCI3ODg0ODQyMDE4MzA5NzY5NTg2MDY2Mjc5NDAwNjAzNjIzNjE4NzcxNTc3MjQ1NDk5NzQ5MzE0MzgyNTUyMzMzMjE4MzA3ODk1NzU4MDk2OTc3NjUzNjQ3MDcwNjk5MDE0OTY4OTUwMDg0OTk2MjMwNDAwODA4OTM0MzQ1MzQwMDcyMzY2NDg5NDYxNDg4MDk0MDgyOTk1OTU3MjUwMTg5NTkxMjg2NDQyMDg4MTMwNDA5MDA4Mjc5MzgxMjUzMDIxOTE0MTc5Nzc5MTAyNTcwNjIxNDQ0MDU0NzcxNjY5Mjk5NjQzNTcwODg5NjY1ODQzOTY2ODA1MjM1ODgxNzQ1OTQ3OTQ4NDQ2ODU1MDY4ODU4ODUzMDg2MTQ5NjMxMjA1ODcwMTIzODc1NDg3MTM0NjAxMDQwODA4Njg2MzQ4NDUwOTA0MTI4MTI4Nzk1MjUzMjczMjU3ODc4NjM4MjAxNTcyOTExMDQxNTQ4NDc0MTMzMDMyMTIyMTMyODExNjQ0NjAzNjg0MDU5MDk2ODM2NjU1NjQzMTI2NDU0NTAwNDM2MTgxNjQxMjkyNjQ4MTQ3MjYxODUyNzY5NjIyMzE5Mjk2NjI0NDU3OTg2NzI5NzMwNzE3NDEyNTE2MzEyNjQwNTM0OTE3NzEwNzE4Njc2MTMwODExMTI2NjQwMTkyODg4NjI2ODI2NTcwNzA1OTUxNTUyODI5NDY2NzY5NjUxNTcxNTI2OTMzNDUyNjY0ODk5NTExNzM0ODk0Njc3OTY5NjI0OTgzODI3MTgzMDg2NjA0NTE0NDE3MDE2MDgxNDE2Nzk0NDgwMDIwNDU2ODMxNzUyNjM2NTk1NzcwNjgwODQ0MDE0MjIyOTc2MjE5NzIzODg1MjAxNTg1ODk0MDQwMDA3MTQ5MjkwMDAxNTc4MjMxMDQiXSxbIm5hbWUiLCI4NjIxNzg3ODk1MzA5MzExOTQ3Njc0OTU5NDA4MjIzMTg5ODgwNDEzNTQ2NzIyOTYyODg5NjI0NzgwMjE1MDc4NDc3OTMxNTk5MDk5MzIwODkyNzY2NjM4NjExNDYyOTMzNTg3NjgwMTU0ODQ4ODgxMTY5MzY5NTc3OTk1NTI1ODQ2NDA1NjcyNDUyMzIyMTcyNjQ4MTc4OTEwMTg2ODkwNzYwNzM2MjMwNzA0MDA3NzU1OTA0OTIyNTUwODQ0MjkxNzgwOTk1NDAyNzUyNTU0NTAwNjg1NTY5NzYzMDc4ODY5NDU0NDI3NzY5NzU0Mzc5ODg5NzAzODQzNDM4ODcyMjMyOTc0MjIzODc5MzY5MTYzOTI1NjY3NjY5MzQyOTUwOTk5MjMwOTY1NDQ1MTkwOTM5Mzk5NzM1NjE1MTk2OTY2MzUyMTMwMzQ5MTE0NDE5OTIwMTk3NDIyMjA0ODQ2MTc2OTI3NTMwMDQ3NDkxNjI1NzAwODQ5NDc1MzQzNzk3MjU0MDYwNjc3MTA4MzkxOTU3MzU0MDAzOTAyNzMzMzEzMDI1ODE5Njk3MTIzMTc2NTg3MTU5NzQ5ODkxODg5MzU3Mjk0OTUyNDMwMDY2MTE5MjgzNzA1NTAwMTcxNTc3ODMzMzk3OTE1Mzc5OTA2NTA1MDExMjczODM2NTM3OTA2NjkyMjg2MTk5NDgzMjA3NDc1MzM2MjE1Njc4MTA1NDY5MDc3MTMyNDAwNDM4NTgyOTAyMjMwMzI5MDc4NjA4NTI4NDgzNDEwODI1NDkzNDcwMjI5MjA3MzA1NTk0ODUwMTg4ODUwMjEyNTQ0NzI0NDgyOTExOTQ3NTQ4NzIzMzMxMzYyNTI3MjA0Mzg3MzE1Mjk1MDU4Njk5MDk3NDc3MjQzMTczOTE0Njk5MDAzOTkzNTQiXV19LCJub25jZSI6IjM2MDY2NjE3NzQwNTA3NzY5NTI5NTMxNyJ9"},"format":"anoncreds/credential-offer@v1.0"}],"thid":"3a1c143b-7ab7-470d-99cf-bc5f31771388","ack":[]}"""
        )

        val expectedAttachmentDataString =
            """{"schema_id":"http://192.168.68.113:8000/cloud-agent/schema-registry/schemas/5667190d-640c-36af-a9f1-f4ed2587e766/schema","cred_def_id":"http://192.168.68.113:8000/cloud-agent/credential-definition-registry/definitions/3da6a1d8-2b23-3138-b02a-ab0bb48f80f9/definition","key_correctness_proof":{"c":"34214338909098159822507248697652522375736834935084639163552680277242971829850","xz_cap":"800707471752947125421556825862479715914917818568025659395472103344600028758373109284713489085997719470979045707565808073761224927372095156301817383419718734496040275336184823748584783621715540927794842106435410778086885179343213257596836659760128729829196324240382977373657104649618254033098674991320376512152997384399364977038756870957572034534565039964163483412113774709156571271527866740755776323981519713645248679782937572928425744617230796360719007374880526808823886530316382925071963250382758009295325483822584922090170101969999185301005436262028421904659368957942500491729264133319784420644451152606026085090463703143473171287951134535064474649419353048340998166660492019842456439171636","xr_cap":[["age","720662800370023933960365144516641427050483880036121464720586097318814491699091752265838768517039207229415105089567415496003203438631336413004072984027137267852461465147145458838032693084467878730776789666951606567700724201024200058868117345168855901024258218816915265076878163228058282563726475010755644358273042121752898251930774480156256234357275841367576484096916751787269606503605538008667265301212109610994218563689701727948761247377845129121748784197562607322484522843599422618507542820798930021132993864131019927051460685941346571524269487489040349935192106893081586983946211402623736990031610476000079667195350127112723873942427684289064046766141384937971077168475856576606878066462728"],["master_secret","788484201830976958606627940060362361877157724549974931438255233321830789575809697765364707069901496895008499623040080893434534007236648946148809408299595725018959128644208813040900827938125302191417977910257062144405477166929964357088966584396680523588174594794844685506885885308614963120587012387548713460104080868634845090412812879525327325787863820157291104154847413303212213281164460368405909683665564312645450043618164129264814726185276962231929662445798672973071741251631264053491771071867613081112664019288862682657070595155282946676965157152693345266489951173489467796962498382718308660451441701608141679448002045683175263659577068084401422297621972388520158589404000714929000157823104"],["name","862178789530931194767495940822318988041354672296288962478021507847793159909932089276663861146293358768015484888116936957799552584640567245232217264817891018689076073623070400775590492255084429178099540275255450068556976307886945442776975437988970384343887223297422387936916392566766934295099923096544519093939973561519696635213034911441992019742220484617692753004749162570084947534379725406067710839195735400390273331302581969712317658715974989188935729495243006611928370550017157783339791537990650501127383653790669228619948320747533621567810546907713240043858290223032907860852848341082549347022920730559485018885021254472448291194754872333136252720438731529505869909747724317391469900399354"]]},"nonce":"360666177405077695295317"}"""
        val attachments = message.attachments
        val attachmentDataString = attachments.first().data.getDataAsJsonString()
        assertNotNull(attachmentDataString)
        assertEquals(expectedAttachmentDataString, attachmentDataString)

        val expectedJsonData =
            """{"options":{"domain":"https://prism-verifier.com","challenge":"11c91493-01b3-4c4d-ac36-b336bab5bddf"},"presentation_definition":{"purpose":null,"format":null,"name":null,"input_descriptors":[],"id":"56108c4a-ca57-40b6-89f0-1e1a2fa186fd"}}"""
        val requestPresentationJson =
            """{"id":"581f9d51-bb0c-4bcd-a851-487a14d30cc6","piuri":"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation","from":{"method":"peer","methodId":"2.Ez6LScuoWiuQHfk4Js2aMC4Qs8rD5zNUfmiNfWMCb2pWR3FAc.Vz6MkvKtf2JqqcxhC1MPmWbWPrxqt8A4v44zri36XHgNmsmgV.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},"to":{"method":"peer","methodId":"2.Ez6LSmuL2dNc6wg5HpqDcDBXnNDG6TawcrBuxZbFkW9Hberjq.Vz6MkvcyMv5VAbTTttvNpo3YYku9Y8VMR9kRw8SFjAX8ic7JU.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},"fromPrior":"null","body":{"goal_code":"Request Proof Presentation","comment":null,"proof_types":[],"will_confirm":false},"createdTime":"1718228880","expiresTimePlus":"2024-06-13T21:48:02.636794Z","attachments":[{"id":"e784ac49-bb2d-4445-9cac-edd365664c73","data":{"type":"org.hyperledger.identus.walletsdk.domain.models.AttachmentJsonData","data":"{\"options\":{\"domain\":\"https://prism-verifier.com\",\"challenge\":\"11c91493-01b3-4c4d-ac36-b336bab5bddf\"},\"presentation_definition\":{\"purpose\":null,\"format\":null,\"name\":null,\"input_descriptors\":[],\"id\":\"56108c4a-ca57-40b6-89f0-1e1a2fa186fd\"}}"},"format":"prism/jwt"}],"thid":"c7cdff93-2706-4023-8d3c-3ce850ab0b2d","ack":[]}"""
        val requestPresentation = json.decodeFromString<RequestPresentation>(requestPresentationJson)
        val attachmentDataString1 = requestPresentation.attachments.first().data.getDataAsJsonString()
        assertNotNull(attachmentDataString1)
        assertEquals(expectedJsonData, attachmentDataString1)

        val message1 = Message(
            piuri = ProtocolType.DidcommRequestPresentation.value,
            body = "",
            attachments = arrayOf(
                AttachmentDescriptor(
                    data = AttachmentData.AttachmentHeader("")
                )
            )
        )
        val attachments1 = message1.attachments
        assertFailsWith<EdgeAgentError.AttachmentTypeNotSupported> {
            attachments1.first().data.getDataAsJsonString()
        }
    }

    @Test
    fun `test connectionless Presentation request with expired invitations`() = runTest {
        val agent = spy(
            EdgeAgent(
                apollo = apolloMock,
                castor = castorMock,
                pluto = plutoMock,
                mercury = mercuryMock,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = null,
                logger = LoggerMock()
            )
        )

        val outOfBandUrl =
            "https://my.domain.com/path?_oob=eyJpZCI6IjViMjUwMjIzLWExNDItNDRmYi1hOWJkLWU1MjBlNGI0ZjQzMiIsInR5cGUiOiJodHRwczovL2RpZGNvbW0ub3JnL291dC1vZi1iYW5kLzIuMC9pbnZpdGF0aW9uIiwiZnJvbSI6ImRpZDpwZWVyOjIuRXo2TFNkV0hWQ1BFOHc0NWZETjM4aUh0ZFJ6WGkyTFNqQmRSUjRGTmNOUm12VkNKcy5WejZNa2Z2aUI5S1F1OGlnNVZpeG1HZHM3dmdMNmoyUXNOUGFybkZaanBNQ0E5aHpQLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQTZMeTh4T1RJdU1UWTRMakV1TXpjNk9EQTNNQzlrYVdSamIyMXRJaXdpY2lJNlcxMHNJbUVpT2xzaVpHbGtZMjl0YlM5Mk1pSmRmWDAiLCJib2R5Ijp7ImdvYWxfY29kZSI6InByZXNlbnQtdnAiLCJnb2FsIjoiUmVxdWVzdCBwcm9vZiBvZiB2YWNjaW5hdGlvbiBpbmZvcm1hdGlvbiIsImFjY2VwdCI6W119LCJhdHRhY2htZW50cyI6W3siaWQiOiIyYTZmOGM4NS05ZGE3LTRkMjQtOGRhNS0wYzliZDY5ZTBiMDEiLCJtZWRpYV90eXBlIjoiYXBwbGljYXRpb24vanNvbiIsImRhdGEiOnsianNvbiI6eyJpZCI6IjI1NTI5MTBiLWI0NmMtNDM3Yy1hNDdhLTlmODQ5OWI5ZTg0ZiIsInR5cGUiOiJodHRwczovL2RpZGNvbW0uYXRhbGFwcmlzbS5pby9wcmVzZW50LXByb29mLzMuMC9yZXF1ZXN0LXByZXNlbnRhdGlvbiIsImJvZHkiOnsiZ29hbF9jb2RlIjoiUmVxdWVzdCBQcm9vZiBQcmVzZW50YXRpb24iLCJ3aWxsX2NvbmZpcm0iOmZhbHNlLCJwcm9vZl90eXBlcyI6W119LCJhdHRhY2htZW50cyI6W3siaWQiOiJiYWJiNTJmMS05NDUyLTQzOGYtYjk3MC0yZDJjOTFmZTAyNGYiLCJtZWRpYV90eXBlIjoiYXBwbGljYXRpb24vanNvbiIsImRhdGEiOnsianNvbiI6eyJvcHRpb25zIjp7ImNoYWxsZW5nZSI6IjExYzkxNDkzLTAxYjMtNGM0ZC1hYzM2LWIzMzZiYWI1YmRkZiIsImRvbWFpbiI6Imh0dHBzOi8vcHJpc20tdmVyaWZpZXIuY29tIn0sInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlkIjoiMGNmMzQ2ZDItYWY1Ny00Y2E1LTg2Y2EtYTA1NTE1NjZlYzZmIiwiaW5wdXRfZGVzY3JpcHRvcnMiOltdfX19LCJmb3JtYXQiOiJwcmlzbS9qd3QifV0sInRoaWQiOiI1YjI1MDIyMy1hMTQyLTQ0ZmItYTliZC1lNTIwZTRiNGY0MzIiLCJmcm9tIjoiZGlkOnBlZXI6Mi5FejZMU2RXSFZDUEU4dzQ1ZkROMzhpSHRkUnpYaTJMU2pCZFJSNEZOY05SbXZWQ0pzLlZ6Nk1rZnZpQjlLUXU4aWc1Vml4bUdkczd2Z0w2ajJRc05QYXJuRlpqcE1DQTloelAuU2V5SjBJam9pWkcwaUxDSnpJanA3SW5WeWFTSTZJbWgwZEhBNkx5OHhPVEl1TVRZNExqRXVNemM2T0RBM01DOWthV1JqYjIxdElpd2ljaUk2VzEwc0ltRWlPbHNpWkdsa1kyOXRiUzkyTWlKZGZYMCJ9fX1dLCJjcmVhdGVkX3RpbWUiOjE3MjQzMzkxNDQsImV4cGlyZXNfdGltZSI6MTcyNDMzOTQ0NH0"
        assertFailsWith(EdgeAgentError.ExpiredInvitation::class) {
            agent.parseInvitation(outOfBandUrl)
        }
    }

    @Test
    fun `test connectionless Presentation request correctly`() = runTest {
        val agent = spy(
            EdgeAgent(
                apollo = apolloMock,
                castor = castorMock,
                pluto = plutoMock,
                mercury = mercuryMock,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = null,
                logger = LoggerMock()
            )
        )

        val notExpiredTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)
        val notExpiredInvitation =
            """{"id":"5b250223-a142-44fb-a9bd-e520e4b4f432","type":"https://didcomm.org/out-of-band/2.0/invitation","from":"did:peer:2.Ez6LSdWHVCPE8w45fDN38iHtdRzXi2LSjBdRR4FNcNRmvVCJs.Vz6MkfviB9KQu8ig5VixmGds7vgL6j2QsNParnFZjpMCA9hzP.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjEuMzc6ODA3MC9kaWRjb21tIiwiciI6W10sImEiOlsiZGlkY29tbS92MiJdfX0","body":{"goal_code":"present-vp","goal":"Request proof of vaccination information","accept":[]},"attachments":[{"id":"2a6f8c85-9da7-4d24-8da5-0c9bd69e0b01","media_type":"application/json","data":{"json":{"id":"2552910b-b46c-437c-a47a-9f8499b9e84f","type":"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation","body":{"goal_code":"Request Proof Presentation","will_confirm":false,"proof_types":[]},"attachments":[{"id":"babb52f1-9452-438f-b970-2d2c91fe024f","media_type":"application/json","data":{"json":{"options":{"challenge":"11c91493-01b3-4c4d-ac36-b336bab5bddf","domain":"https://prism-verifier.com"},"presentation_definition":{"id":"0cf346d2-af57-4ca5-86ca-a0551566ec6f","input_descriptors":[]}}},"format":"prism/jwt"}],"thid":"5b250223-a142-44fb-a9bd-e520e4b4f432","from":"did:peer:2.Ez6LSdWHVCPE8w45fDN38iHtdRzXi2LSjBdRR4FNcNRmvVCJs.Vz6MkfviB9KQu8ig5VixmGds7vgL6j2QsNParnFZjpMCA9hzP.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjEuMzc6ODA3MC9kaWRjb21tIiwiciI6W10sImEiOlsiZGlkY29tbS92MiJdfX0"}}}],"created_time":1724339144,"expires_time":$notExpiredTime}"""
        val base64Invitation = notExpiredInvitation.base64UrlEncoded
        doReturn(DID("did:peer:asdf")).`when`(agent).createNewPeerDID(updateMediator = true)

        val outOfBandUrl = "https://my.domain.com/path?_oob=$base64Invitation"
        val connectionlessRequestPresentation = agent.parseInvitation(outOfBandUrl)
        assertTrue(connectionlessRequestPresentation is ConnectionlessRequestPresentation)
        val msg = (connectionlessRequestPresentation as ConnectionlessRequestPresentation).requestPresentation

        assertEquals("5b250223-a142-44fb-a9bd-e520e4b4f432", msg.thid)
        val attachments = msg.attachments
        assertEquals(1, attachments.size)
        val attachmentJsonData = attachments.first().data
        assertTrue(attachmentJsonData is AttachmentData.AttachmentJsonData)
        val json = Json.parseToJsonElement(attachmentJsonData.getDataAsJsonString())
        assertTrue(json.jsonObject.containsKey("options"))
        assertTrue(json.jsonObject.containsKey("presentation_definition"))
    }

    @Test
    fun `test connectionless credential offer correctly`() = runTest {
        val agent = spy(
            EdgeAgent(
                apollo = apolloMock,
                castor = castorMock,
                pluto = plutoMock,
                mercury = mercuryMock,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = null,
                logger = LoggerMock()
            )
        )

        val notExpiredTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)
        val notExpiredInvitation =
            """{"id":"f96e3699-591c-4ae7-b5e6-6efe6d26255b","type":"https://didcomm.org/out-of-band/2.0/invitation","from":"did:peer:2.Ez6LSfsKMe8vSSWkYdZCpn4YViPERfdGAhdLAGHgx2LGJwfmA.Vz6Mkpw1kSabBMzkA3v59tQFnh3FtkKy6xLhLxd9S6BAoaBg2.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjEuMzc6ODA4MC9kaWRjb21tIiwiciI6W10sImEiOlsiZGlkY29tbS92MiJdfX0","body":{"goal_code":"issue-vc","goal":"To issue a Faber College Graduate credential","accept":["didcomm/v2"]},"attachments":[{"id":"70cdc90c-9a99-4cda-87fe-4f4b2595112a","media_type":"application/json","data":{"json":{"id":"655e9a2c-48ed-459b-b3da-6b3686655564","type":"https://didcomm.org/issue-credential/3.0/offer-credential","body":{"goal_code":"Offer Credential","credential_preview":{"type":"https://didcomm.org/issue-credential/3.0/credential-credential","body":{"attributes":[{"name":"familyName","value":"Wonderland"},{"name":"givenName","value":"Alice"},{"name":"drivingClass","value":"Mw==","media_type":"application/json"},{"name":"dateOfIssuance","value":"2020-11-13T20:20:39+00:00"},{"name":"emailAddress","value":"alice@wonderland.com"},{"name":"drivingLicenseID","value":"12345"}]}}},"attachments":[{"id":"8404678b-9a36-4989-af1d-0f445347e0e3","media_type":"application/json","data":{"json":{"options":{"challenge":"ad0f43ad-8538-41d4-9cb8-20967bc685bc","domain":"domain"},"presentation_definition":{"id":"748efa58-2bce-440d-921f-2520a8446663","input_descriptors":[],"format":{"jwt":{"alg":["ES256K"],"proof_type":[]}}}}},"format":"prism/jwt"}],"thid":"f96e3699-591c-4ae7-b5e6-6efe6d26255b","from":"did:peer:2.Ez6LSfsKMe8vSSWkYdZCpn4YViPERfdGAhdLAGHgx2LGJwfmA.Vz6Mkpw1kSabBMzkA3v59tQFnh3FtkKy6xLhLxd9S6BAoaBg2.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjEuMzc6ODA4MC9kaWRjb21tIiwiciI6W10sImEiOlsiZGlkY29tbS92MiJdfX0"}}}],"created_time":1724851139,"expires_time":$notExpiredTime}"""
        val base64Invitation = notExpiredInvitation.base64UrlEncoded
        doReturn(DID("did:peer:asdf")).`when`(agent).createNewPeerDID(updateMediator = true)

        val outOfBandUrl = "https://my.domain.com/path?_oob=$base64Invitation"
        val connectionlessCredentialOffer = agent.parseInvitation(outOfBandUrl)
        assertTrue(connectionlessCredentialOffer is ConnectionlessCredentialOffer)
        connectionlessCredentialOffer as ConnectionlessCredentialOffer
        val msg = connectionlessCredentialOffer.offerCredential.makeMessage()

        assertEquals(ProtocolType.DidcommOfferCredential.value, msg.piuri)
        assertEquals("f96e3699-591c-4ae7-b5e6-6efe6d26255b", msg.thid)
        val attachments = msg.attachments
        assertEquals(1, attachments.size)
        val attachmentJsonData = attachments.first().data
        assertTrue(attachmentJsonData is AttachmentData.AttachmentJsonData)
        val json = Json.parseToJsonElement(attachmentJsonData.getDataAsJsonString())
        assertTrue(json.jsonObject.containsKey("options"))
        assertTrue(json.jsonObject["options"]!!.jsonObject.containsKey("challenge"))
        assertTrue(json.jsonObject["options"]!!.jsonObject.containsKey("domain"))
        assertTrue(json.jsonObject.containsKey("presentation_definition"))
        assertTrue(json.jsonObject["presentation_definition"]!!.jsonObject.containsKey("id"))
        assertTrue(json.jsonObject["presentation_definition"]!!.jsonObject.containsKey("input_descriptors"))
        assertTrue(json.jsonObject["presentation_definition"]!!.jsonObject.containsKey("format"))
        assertTrue(json.jsonObject["presentation_definition"]!!.jsonObject["format"]!!.jsonObject.contains("jwt"))
        assertTrue(
            json.jsonObject["presentation_definition"]!!.jsonObject["format"]!!.jsonObject["jwt"]!!.jsonObject.contains(
                "alg"
            )
        )
        val algs =
            json.jsonObject["presentation_definition"]!!.jsonObject["format"]!!.jsonObject["jwt"]!!.jsonObject["alg"]!!.jsonArray
        assertEquals("ES256K", algs.first().jsonPrimitive.content)
        assertTrue(
            json.jsonObject["presentation_definition"]!!.jsonObject["format"]!!.jsonObject["jwt"]!!.jsonObject.contains(
                "proof_type"
            )
        )
    }

    val getCredentialDefinitionResponse =
        "{\"schemaId\":\"http://host.docker.internal:8000/prism-agent/schema-registry/schemas/5e0d5a93-4bfd-3111-a956-5d5bc82f76cc\",\"type\":\"CL\",\"tag\":\"licence\",\"value\":{\"primary\":{\"n\":\"105195159277979097653318357586659371305119697478469834190626350283715795188687389523188659352120689851168860621983864738336838773213022505168653440146374011050277159372491059901432822905781969400722059341786498751125483895348734607382548396665339315322605154516776326303787844694026898270194867398625429469096229269732265502538641116512214652017416624138065704599041020588805936844771273861390913500753293895219370960892829297672575154196820931047049021760519166121287056337193413235473255257349024671869248216238831094979209384406168241010010012567685965827447177652200129684927663161550376084422586141212281146491949\",\"s\":\"85376740935726732134199731472843597191822272986425414914465211197069650618238336366149699822721009443794877925725075553195071288777117865451699414058058985000654277974066307286552934230286237253977472401290858765904161191229985245519871949378628131263513153683765553672655918133136828182050729012388157183851720391379381006921499997765191873729408614024320763554099291141052786589157823043612948619201525441997065264492145372001259366749278235381762443117203343617927241093647322654346302447381494008414208398219626199373278313446814209403507903682881070548386699522575055488393512785511441688197244526708647113340516\",\"r\":{\"dateofissuance\":\"16159515692057558658031632775257139859912833740243870833808276956469677196577164655991169139545328065546186056342530531355718904597216453319851305621683589202769847381737819412615902541110462703838858425423753481085962114120185123089078513531045426316918036549403698066078445947881055316312848598741184161901260446303171175343050250045452903485086185722998336149005743485268486377824763449026501058416292877646187105446333888525480394665310217044483841168928926515929150167890936706159800372381200383816724043496032886366767166850459338411710056171379538841845247931898550165532492578625954615979453881721709564750235\",\"drivingclass\":\"83649701835078373520097916558245060224505938113940626586910000950978790663411517512280043632278010831292224659523658613504637416710001103641231226266903556936380105758523760424939825687213460920436570466066231912959327201876189240504388424799892400351592593406285436824571943165913587899115814843543998396726679289422080229750418336051741708013580146373647528674381958028243228435161765957312248113519708734663989428761879029086059388435772829434952754093999424834120341657211221855300108096057633128467059590470639772605075954658131680801785637700237403873940041665483384938586320674338994185073499523485570537331062\",\"emailaddress\":\"96995643129591814391344614133120459563648002327749700279517548454036811217735867585059116635583558148259032071807493674533230465312311981127622542797279917256478867847832932893748528200469349058284133058865149153179959849308383505167342565738382180666525211256221655129861213392455759272915565057394420728271409215556596974900718332893753172173500744392522771654048192448229319313386967045678744665093451560743782910263014930200762027209565313884859542996067229707388839912195826334964819133016500346618083969320902775088800287566711941842968839787149808739739233388585677095545116231323172342995837636586249573194609\",\"drivinglicenseid\":\"102840929811153624977554462471309185033977661854754815794111114507549576719389525167082631547450413573293352276930065480432301200611396989595571202142654033217842162456070556560693402484110499573693863745648118310258284468114751958738878996458420605301017450868522680454545537837403398645500541915771765220093329728663621098538954397330411649083351383375839056527007892276284168437065687748085384178113959961057476582871100422859953560730152958588610850909069434658487744782540788968302663076149478487413357533660817020800754493642858564081116318655661240523146995256712471572605700346459123074377380656921337264554594\",\"familyname\":\"2428690037146701497427424649573806616639612325136606164619283916796880313617677563507218774958436668407050506838114136163250163675016510113975582318007560622124292458766639319715064358235569650961433812439763343736699708535945693241909905707497180931492818502593885932421170612418693515054756633264933222189766691632082890045477718331705366111669009551578289182848340651375008362238266590844461708981816856194045325523248527964502118319210042254240848590574645476930113881493472578612352948284862674703949781070309344526122291448990325949065193279599181502524961004046979227803224474342778516917124487012958845744311\",\"master_secret\":\"96236339155824229583363924057798366491998077727991424922911165403434522806469328114407334094535810942859512352089785125683335350062474092708044674085769524387654467267128528564551803293661877480971961092735622606052503557881856409855812611523475975566606131897917979412576797874632169829901968854843162299366867885636535326810998541141840561418097240137120398317445832694001031827068485975315937269024666370665530455146256019590700349556357390218401217383173228376078058967743472704019765210324846681867991543267171763037513180046865961560351035005185946817643006206395175857900512245900162751815626427008481585714891\"},\"rctxt\":\"54359809198312125478916383106913469635175253891208897419510030559787479974126666313900084654632259260010008369569778456071591398552341004538623276997178295939490854663263886825856426285604332554317424030793691008221895556474599466123873279022389276698551452690414982831059651505731449763128921782866843113361548859434294057249048041670761184683271568216202174527891374770703485794299697663353847310928998125365841476766767508733046891626759537001358973715760759776149482147060701775948253839125589216812475133616408444838011643485797584321993661048373877626880635937563283836661934456534313802815974883441215836680800\",\"z\":\"99592262675748359673042256590146366586480829950402370244401571195191609039150608482506917768910598228167758026656953725016982562881531475875469671976107506976812319765644401707559997823702387678953647104105378063905395973550729717937712350758544336716556268064226491839700352305793370980462034813589488455836259737325502578253339820590260554457468082536249525493340350556649403477875367398139579018197084796440810685458274393317299082017275568964540311198115802021902455672385575542594821996060452628805634468222196284384514736044680778624637228114693554834388824212714580770066729185685978935409859595244639193538156\"}},\"issuerId\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550\"}"

    val getLinkSecret =
        "\"40960368705011900020984253500979753785333739067716460047393316855560515114468\""
    val getDids = """
        [{"method":"peer","methodId":"2.Ez6LSok96TA4orHQXSMHZj3mqyUuVLMfLfGGqj27i1giErbXL.Vz6Mku5mY1GuJ9AN2vvDwjMv5QUC2zqKVRPCcbmJVYTFTCFmr"},{"method":"peer","methodId":"2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHBzOi8vc2l0LXByaXNtLW1lZGlhdG9yLmF0YWxhcHJpc20uaW8iLCJhIjpbImRpZGNvbW0vdjIiXX19.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6IndzczovL3NpdC1wcmlzbS1tZWRpYXRvci5hdGFsYXByaXNtLmlvL3dzIiwiYSI6WyJkaWRjb21tL3YyIl19fQ"},{"method":"peer","methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},{"method":"prism","methodId":"6f23ddace519b68dfc0fa06e992db40f2f3c584af382ce446fa2fd0e042e5dea:CoUBCoIBEjsKB21hc3RlcjAQAUouCglzZWNwMjU2azESIQMcKwEitGbQKtGa-jFXi3m1u7OP2JMukYXQnZk3fQIXvxJDCg9hdXRoZW50aWNhdGlvbjAQBEouCglzZWNwMjU2azESIQMcKwEitGbQKtGa-jFXi3m1u7OP2JMukYXQnZk3fQIXvw"},{"method":"prism","methodId":"0a4b552169e3158781741fbbeffe81212784d32d90cf8f2622923f11f6ecd966:CoUBCoIBEjsKB21hc3RlcjAQAUouCglzZWNwMjU2azESIQLgzhsuOqhAyImy-c8o9ZmIJ4iY_Gc8tvNIT3l1w58f2BJDCg9hdXRoZW50aWNhdGlvbjAQBEouCglzZWNwMjU2azESIQLgzhsuOqhAyImy-c8o9ZmIJ4iY_Gc8tvNIT3l1w58f2A"}]
        """
    val getDidPairs = """
        [{"holder":{"method":"peer", "methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"}, "receiver":{"method":"peer", "methodId":"2.Ez6LSrhfy5nfumryQUhCU9CRFZvEy3zZV2pUedwpUoYbeiZbq.Vz6MksfNehMay3PPDQEfaaeCotpgC5z9hCnbF6s8uYWUGjSYh.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"}, "name":""}]
        """
    val getPrivateKeys = """
        [
            {
                "key":"{\"kty\":\"OKP\",\"d\":\"j1BaK2P1iHMt9hwb6GzZCcpjkKrYmBPX5LPI4aFLTT0\",\"crv\":\"Ed25519\",\"x\":\"2V3e1LsSkcjwvlX2Y9Fp2jbg_j5lan-nZJRxYy_sUjk\"}",
                "did":"did:peer:2.Ez6LSok96TA4orHQXSMHZj3mqyUuVLMfLfGGqj27i1giErbXL.Vz6Mku5mY1GuJ9AN2vvDwjMv5QUC2zqKVRPCcbmJVYTFTCFmr#key-2",
                "index":0,
                "recovery_id":"ed25519+priv"
            },
            {
                "key":"{\"kty\":\"OKP\",\"d\":\"6BQsylDTirk7C6bacJuaH28tx6jXmNZv7LojNN2Tgnw\",\"crv\":\"X25519\",\"x\":\"s1A5vv8L6_8NhtPr7L_CaE72WVkt6UNnoj2mtqS11H8\"}",
                "did":"did:peer:2.Ez6LSok96TA4orHQXSMHZj3mqyUuVLMfLfGGqj27i1giErbXL.Vz6Mku5mY1GuJ9AN2vvDwjMv5QUC2zqKVRPCcbmJVYTFTCFmr#key-1",
                "index":0,
                "recovery_id":"x25519+priv"
            },
            {
                "key":"{\"kty\":\"OKP\",\"d\":\"wE2pfN2Y0iwqa2py22NKZJvvq7nZZ_Ff9yYyvBTUhig\",\"crv\":\"Ed25519\",\"x\":\"fDrwZceyjKWYnvstPyk_b353dgOQ8_YOs1TK12h0trE\"}",
                "did":"did:peer:2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ#key-2",
                "index":0,
                "recovery_id":"ed25519+priv"
            },
            {
                "key":"{\"kty\":\"OKP\",\"d\":\"IMLHcfmNSp6A4ZSqpxModc3maH5O-k3Jwpt1PeMkGXU\",\"crv\":\"X25519\",\"x\":\"zZOgc8btEWstmzAlrFmHZAQB7z9OmsWUMqdMlLnEuSk\"}",
                "did":"did:peer:2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ#key-1",
                "index":0,
                "recovery_id":"x25519+priv"
            },
            {
                "key":"{\"kty\":\"EC\",\"d\":\"drF7QtIgnKGMpQTm2BxomRFeKkgHWV4r2lfYCVUYPns\",\"crv\":\"secp256k1\",\"x\":\"HCsBIrRm0CrRmvoxV4t5tbuzj9iTLpGF0J2ZN30CF78\",\"y\":\"MuTzhhijGiKuc_Ajm7iasGJwSETvhjK7SEukKBLMMOM\"}",
                "did":"did:prism:6f23ddace519b68dfc0fa06e992db40f2f3c584af382ce446fa2fd0e042e5dea:CoUBCoIBEjsKB21hc3RlcjAQAUouCglzZWNwMjU2azESIQMcKwEitGbQKtGa-jFXi3m1u7OP2JMukYXQnZk3fQIXvxJDCg9hdXRoZW50aWNhdGlvbjAQBEouCglzZWNwMjU2azESIQMcKwEitGbQKtGa-jFXi3m1u7OP2JMukYXQnZk3fQIXvw",
                "index":1,
                "recovery_id":"secp256k1+priv"
            },
            {
                "key":"{\"kty\":\"EC\",\"d\":\"QPFq8rgWb8jDBBVOpR9_tnzy0mW1RwMLBL5x1DUle-k\",\"crv\":\"secp256k1\",\"x\":\"4M4bLjqoQMiJsvnPKPWZiCeImPxnPLbzSE95dcOfH9g\",\"y\":\"vld5MJHizoXDITRfAjkFiKs8PAbGbM21cNCfzFyanYQ\"}",
                "did":"did:prism:0a4b552169e3158781741fbbeffe81212784d32d90cf8f2622923f11f6ecd966:CoUBCoIBEjsKB21hc3RlcjAQAUouCglzZWNwMjU2azESIQLgzhsuOqhAyImy-c8o9ZmIJ4iY_Gc8tvNIT3l1w58f2BJDCg9hdXRoZW50aWNhdGlvbjAQBEouCglzZWNwMjU2azESIQLgzhsuOqhAyImy-c8o9ZmIJ4iY_Gc8tvNIT3l1w58f2A",
                "index":2,
                "recovery_id":"secp256k1+priv"
            }
        ]
        """
    val getCredentials = """
        [
            {
                "restorationId":"jwt+credential",
                "credentialData":"ZXlKaGJHY2lPaUpGVXpJMU5rc2lmUS5leUpwYzNNaU9pSmthV1E2Y0hKcGMyMDZNek0zWlRobVpURTBOR0ZoWTJWa00yTmhNMlJrTlRrME5qSTBNRFJtTkRVNU9UWmxNMkl5TWpGaFltTTBNVEJoTnpJMVpXRTJOalV6TkRnNU56SmlZanBEY210Q1EzSlpRa1ZxYjB0Q2JVWXhaRWRuZEUxU1FVVlRhVFJMUTFoT2JGa3pRWGxPVkZweVRWSkphRUYyZVdjeFlUTjFjSFZtYkZCTGN6aEtSMWhLVTNOeFYxcGpWRzlHUVhrM1JqTlNURkJqUWxrMFYyNXpSV3B6UzBJeWJIcGpNMVpzVEZSRlVVRnJiM1ZEWjJ4NldsZE9kMDFxVlRKaGVrVlRTVkZQWVZCVWJ6TTVUbmgyVW1oWFVXNWlWV2hvVFhNNWJURkllRUp0Y1Y5aFpXTkhNMHRUVEdaaU5XZ3pVa2szUTJka2RGbFlUakJhV0VsM1JVRkdTMHhuYjBwak1sWnFZMFJKTVU1dGMzaEZhVVZFWjNwb09FbERZMVpoTlZsTlpqWXpSRkZhTTE5MWRUTk9Nek5zU1hWR1NHSm9YMDlLVWxWSWJXZDJZeUlzSW5OMVlpSTZJbVJwWkRwd2NtbHpiVG93WVRSaU5UVXlNVFk1WlRNeE5UZzNPREUzTkRGbVltSmxabVpsT0RFeU1USTNPRFJrTXpKa09UQmpaamhtTWpZeU1qa3lNMll4TVdZMlpXTmtPVFkyT2tOdlZVSkRiMGxDUldwelMwSXlNV2hqTTFKc1kycEJVVUZWYjNWRFoyeDZXbGRPZDAxcVZUSmhla1ZUU1ZGTVozcG9jM1ZQY1doQmVVbHRlUzFqT0c4NVdtMUpTalJwV1Y5SFl6aDBkazVKVkROc01YYzFPR1l5UWtwRVEyYzVhR1JZVW05YVZ6VXdZVmRPYUdSSGJIWmlha0ZSUWtWdmRVTm5iSHBhVjA1M1RXcFZNbUY2UlZOSlVVeG5lbWh6ZFU5eGFFRjVTVzE1TFdNNGJ6bGFiVWxLTkdsWlgwZGpPSFIyVGtsVU0yd3hkelU0WmpKQklpd2libUptSWpveE56RTRNek0wTVRVeExDSjJZeUk2ZXlKamNtVmtaVzUwYVdGc1UzVmlhbVZqZENJNmV5SmxiV0ZwYkVGa1pISmxjM01pT2lKa1pXMXZRR1Z0WVdsc0xtTnZiU0lzSW1SeWFYWnBibWREYkdGemN5STZJakVpTENKbVlXMXBiSGxPWVcxbElqb2laR1Z0YnlJc0ltUnlhWFpwYm1kTWFXTmxibk5sU1VRaU9pSkJNVEl5TVRNek1pSXNJbWxrSWpvaVpHbGtPbkJ5YVhOdE9qQmhOR0kxTlRJeE5qbGxNekUxT0RjNE1UYzBNV1ppWW1WbVptVTRNVEl4TWpjNE5HUXpNbVE1TUdObU9HWXlOakl5T1RJelpqRXhaalpsWTJRNU5qWTZRMjlWUWtOdlNVSkZhbk5MUWpJeGFHTXpVbXhqYWtGUlFWVnZkVU5uYkhwYVYwNTNUV3BWTW1GNlJWTkpVVXhuZW1oemRVOXhhRUY1U1cxNUxXTTRiemxhYlVsS05HbFpYMGRqT0hSMlRrbFVNMnd4ZHpVNFpqSkNTa1JEWnpsb1pGaFNiMXBYTlRCaFYwNW9aRWRzZG1KcVFWRkNSVzkxUTJkc2VscFhUbmROYWxVeVlYcEZVMGxSVEdkNmFITjFUM0ZvUVhsSmJYa3RZemh2T1ZwdFNVbzBhVmxmUjJNNGRIWk9TVlF6YkRGM05UaG1Na0VpTENKa1lYUmxUMlpKYzNOMVlXNWpaU0k2SWpBeFhDOHdNVnd2TWpBeU5DSjlMQ0owZVhCbElqcGJJbFpsY21sbWFXRmliR1ZEY21Wa1pXNTBhV0ZzSWwwc0lrQmpiMjUwWlhoMElqcGJJbWgwZEhCek9sd3ZYQzkzZDNjdWR6TXViM0puWEM4eU1ERTRYQzlqY21Wa1pXNTBhV0ZzYzF3dmRqRWlYU3dpWTNKbFpHVnVkR2xoYkZOMFlYUjFjeUk2ZXlKemRHRjBkWE5RZFhKd2IzTmxJam9pVW1WMmIyTmhkR2x2YmlJc0luTjBZWFIxYzB4cGMzUkpibVJsZUNJNk5Td2lhV1FpT2lKb2RIUndPbHd2WEM4eE9USXVNVFk0TGpZNExqRXhNem80TURBd1hDOXdjbWx6YlMxaFoyVnVkRnd2WTNKbFpHVnVkR2xoYkMxemRHRjBkWE5jTHpNNVlqQmlOekkyTFRCbU5tVXRORGxtTnkwNVl6VXlMVFl5WVRjNE1UY3hOelZsT0NNMUlpd2lkSGx3WlNJNklsTjBZWFIxYzB4cGMzUXlNREl4Ulc1MGNua2lMQ0p6ZEdGMGRYTk1hWE4wUTNKbFpHVnVkR2xoYkNJNkltaDBkSEE2WEM5Y0x6RTVNaTR4TmpndU5qZ3VNVEV6T2pnd01EQmNMM0J5YVhOdExXRm5aVzUwWEM5amNtVmtaVzUwYVdGc0xYTjBZWFIxYzF3dk16bGlNR0kzTWpZdE1HWTJaUzAwT1dZM0xUbGpOVEl0TmpKaE56Z3hOekUzTldVNEluMTlmUS5maW5ESHhybHRxbU9CcXBEZ3Zfa0NMVk02dnZCRFU3YWRoY01UV3Y2VTRwMlBha3puc0htbDl2TXpxNGpidWlfTXAwZDFoTm0tUXVVcFRMSUFiY2Z1QQ",
                "revoked": false
            },
            {
                "restorationId":"anon+credential",
                "credentialData":"eyJzY2hlbWFfaWQiOiJodHRwOi8vaG9zdC5kb2NrZXIuaW50ZXJuYWw6ODAwMC9wcmlzbS1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy9mNTJlYmM2MS02MTQ2LTMyMzgtOTljNS1hM2ZkNjkzYjk2Zjcvc2NoZW1hIiwiY3JlZF9kZWZfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9wcmlzbS1hZ2VudC9jcmVkZW50aWFsLWRlZmluaXRpb24tcmVnaXN0cnkvZGVmaW5pdGlvbnMvM2Y0ZWEwOTgtODdiNi0zMDJhLWE3MjYtZjkzZmU5MzhjZjI0L2RlZmluaXRpb24iLCJ2YWx1ZXMiOnsiZW1haWxhZGRyZXNzIjp7InJhdyI6ImNyaXN0aWFuLmNhc3Ryb0Bpb2hrLmlvIiwiZW5jb2RlZCI6IjM2MzQyMzgzNzQ1NTM4Njg0NzkxOTMwNTAzMzYyNTczOTQ0MjQzNzA2MjA3NTE4NzU5MDI1MTY5MTAyODM3MDAxMzM0NjExMzcyNzQzIn19LCJzaWduYXR1cmUiOnsicF9jcmVkZW50aWFsIjp7Im1fMiI6IjEwODg5NjAwNjg2NTY2NzkwOTA2Njc3NTU5ODIxNjY4NjU5NjUyNDE3MzYxNjcwNTYzMzgzNDMwMDEzMDYxNDU4NDMzMzIyNTUxODIwMyIsImEiOiIzNDIxODA5NzM3MzQ2MTYyNDQ5NzIzOTI1NDYxNTg4ODY1NTYzMDg1NTIyNDY0OTY0MDI4NzQwMjAxNzUzNTQ4ODcyNzQ4NDUyMzg4OTM3MDM2Mjk1MzIxOTg4OTU1OTgwMjg4OTMyNTcyNzc4NTgyNDcwODIzNDc3MjQ5NTcwMzA3NTkyNjU3ODQ0MjAyMDY5MDI5Nzk5NjQ2NzI3Mjk2OTQ5OTg4NDMzMDMzMzAzNDUzMDI3NTkzMjc5ODYyMzAwNTg2MzA4MjgyNTI4MjMzODQ5MTAyOTAyODE2MzA1MTEwODE5NTU2MDEyNzU0NDUxMjAzNTAzODUyMDUyMjczMzg1NjIyMTU2NDk5MTYxNzg1Mjc1MDcxNjQ2ODYyNTcwNTk3Nzg1MDU0NDcyNTY2ODUyODc1NjA3NjUzMzQxMDQwODM2Njg5MzAxMjEyOTc3NDkyMjA1MTk0Mjg0MzEzODQyNzMyMzM4NTcwMTgwNjAzMDEyOTA0ODAwMDQxNDIzMTg1ODQ2ODgzNjM2Mjk0MDI0MDczNTQ5MDgzMDg4NTMzOTQ3NDUzMTM1MTkyNzYyODYzNzg3NDE2NTYxNzEyNDczNTE4OTcyNTU2ODYzNjQ4NTYwMjI2NDE2Nzk1MjgyODQ1OTk3MjU4ODkyMjE2OTA2MDcxNzUwODM5NTgwNTg4MzYzMTAxMDY5NjA4MzA3MDIzNzc2NDU5Mzg1MjA2NDQ3ODcxMjU3MzY3MTI4MDIyNzAxODM2MzI2MTEzNTcxMDAyOTc3Njk1MzgwODE2MDE5MzgxOTQxNDQ4MDk0MDU3ODYwNDMwIiwiZSI6IjI1OTM0NDcyMzA1NTA2MjA1OTkwNzAyNTQ5MTQ4MDY5NzU3MTkzODI3Nzg4OTUxNTE1MjMwNjI0OTcyODU4MzEwNTY2NTgwMDcxMzMwNjc1OTE0OTk4MTY5MDU1OTE5Mzk4NzE0MzAxMjM2NzkxMzIwNjI5OTMyMzg5OTY5Njk0MjIxMzIzNTk1Njc0MjkzMDE0MDYwMzc0NDMwNTk3MDc5NzA2NTE4MjI5MjY3MjY2NjI5OSIsInYiOiI5Mzk3ODc5ODYxNTE4MTM5MDkyMTczOTQ0NTk1NTYxNTMzMjM4ODc0MTU2MTEzMTMzMDA2MDg2MDg0Njc1NzM5MDYwODA3MzYxOTM0OTAyMzg1MTYyODM5Mjg5Mjc2NjkwMTYyMzIxOTg4OTA3Njg0Mzk3OTE1Mzk5MTU4OTgxOTAxMTE1ODg0MTE4NDc5NzY1ODQzNjI2MjQzNDg3NjUwMzY5NjY1Mzk2MDc1NDA2NTkwNzQzMzU0NzQzODg3MzU1OTQ4NDY5MzQ0ODE1MTY1NTUyOTE2NzIzNDgzNDQ2MzIzNDIwNzAzNDczNTc4NDgzMDY3Njg3NTc4Njg1NTU2MTM1MDUyMjA3OTUwODA4MjAzMDE4NDkxODI5MjQxMTY3Mjc1Nzg1MzE5NjM4MzM4MTA5MDQyNDI3NjA2NDQ4NjczNDMyMjAyMDEyODQwNzczMTA5ODk4MDI1MTk0NTg0MDAzNTk5MjY2NTgxNzgwNjUwMjcxOTE0NDAzNjA2ODg4MTMwNjgwOTI3NzI2NDM3MDkxNTc4Nzk1MjM3MjUxMzE0Mjg2ODE5ODI4NDkwODg2OTY2NDM1MDcwMDk3MjczMDkzMzE2NjgwMDE2ODcxMTUyMjE0NTExNzM2MzE2ODAxMjY5NDE4OTEzNTA0ODg4NjI5Njg2MTgzNTg1NzA4OTkxNjY0MjE5MjcyNzA1NjEzNzUxMjY2MDExNTUxOTA2ODkwNDU5MDY5MjA5NTk1MTE3OTAzMDI5MTMxNDY4ODg0MjI4MTA2ODcxOTc0NzA3MjI0ODA1MjIzODMyOTMzODMwNzgzNjM3NDM5NDA4MTEzNTMxMjM3NjA3NTMyMzM2ODQ1Nzk2MzA1MjA3Nzk4MDg5MzQzMDc2NDAzMzIyMTEyODk5NDA3ODI1OTA5NDcxOTcwNTk2MDczNTk3MzI0NTg2NDU4MTEyMzc0ODIxMDAwMTIyMzY2NTE5NjE2MDI1NzkyOTI1NTM2OTI3Mjk2NDM0MDExOTI2NDI2Njg3NDYyODQzMDczMzg3NzE5NTczMTIyMjkyNzIwNjcyMTExODE2NTkwMTIwOTEyNTM1OTkyNzk3In19LCJzaWduYXR1cmVfY29ycmVjdG5lc3NfcHJvb2YiOnsic2UiOiIxMjc3MjU5MTIxMDkwMDA1NDExNTY3NDIwNTkwODEyNDQ5MzE1Mzg3ODE4ODU2NjY5MjE5MTAzNTE1NzEwNDY5Nzk5ODIwNDc2OTIxMTMwODQ4Nzg1OTMzNjU2MDE2MDE1NjI3MjExNzU5NTg4Nzc1Mzg5NDc0MTY3NTkyMzE0OTI1NTc4MjgyOTkxMzY5ODM0OTEwMjY4NDA3MDcxNjc3NzUwMjA2NDY4MDAzMjAxMDM3NjU1MDgzNDQ1ODkzNTYwNjM1NjE0MzgwMTQ5MDU0NDExMTg4MjMwNjg1MDA1NDA1ODgwNTE3NjkzNjE2NDk0MTk5NDA2NDQ1MjE2OTIwMDA2Nzk1NzQ5MDQ4NjQwOTMyNTE1NjQ4MTkyNDU0MDk3MTQxNDE3NjY0Nzc0NDMxMzg5MDA1NDUwMTM4NDcyNjQ1NzU3NzMzMjY0MzI4MTQ1MzcyMjcwODgyODIxMjA5Nzg4NjAyMzQ4NDc1OTMzNTU3NzU4MzkzMzMzNTU5ODEyOTQ4MjcyMjcxODEwMzk0OTE0MzQ2NTg5NTU4NjkxMTE0NzgzOTM5Njk1MDgzMjU5MjMwNzU3MTU5MjEwODU3MDY4ODIyNTgzNTY1OTE3ODc4MjQxNjU0MTU4NjY4NjUyMzc2NzAyNzExMTQyMDcwNzA0MDc2MzQ3MzU0NjAyMTI4MTE5MjgyNzQ3NjUxOTg3OTg2MjAwMDgzNzk2OTUwODk1MzExNTA5NDMxNzkwNTcxNzUwMTUxOTkzMjgwMzkyODgyMjE2MTIyMDA5NTc3MzEwMDI4NzYyMDY0MTAwNTg3MTQ0NTMwNiIsImMiOiI5Mzc5MzI3MTk1Mjc5MTYzNzQyMjg2MTE0MzgwMjMzNjcwMzg5NzI5MTcyNjA1MDUwMjQ0MDU5OTI3MDQwNjM1MjA4OTgzNjAyOTkxOSJ9fQ",
                "revoked": false
            }
        ]
        """
    val getMessages = """
        [
    {
        "id":"49d81234-8d78-4c12-8894-ae3a624deaa1",
        "piuri":"https://atalaprism.io/mercury/connections/1.0/request",
        "from":{"method":"peer","methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},
        "to":{"method":"peer","methodId":"2.Ez6LSrhfy5nfumryQUhCU9CRFZvEy3zZV2pUedwpUoYbeiZbq.Vz6MksfNehMay3PPDQEfaaeCotpgC5z9hCnbF6s8uYWUGjSYh.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},
        "fromPrior":null,
        "body":"{\"accept\":[]}",
        "created_time":"1718334045301",
        "expires_time_plus":"1718420445301",
        "attachments":[
            
        ],
        "thid":"635e1e59-54b2-4cce-b635-f70f6a4b0268",
        "ack":[
            
        ],
        "direction":"SENT"
    },
    {
        "id":"76170446-761a-48e5-a869-1800089c6024",
        "piuri":"https://atalaprism.io/mercury/connections/1.0/response",
        "from":{"method":"peer","methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},
        "to":{"method":"peer","methodId":"2.Ez6LSrhfy5nfumryQUhCU9CRFZvEy3zZV2pUedwpUoYbeiZbq.Vz6MksfNehMay3PPDQEfaaeCotpgC5z9hCnbF6s8uYWUGjSYh.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},
        "fromPrior":"null",
        "body":"{\"goal\":null,\"goal_code\":null,\"accept\":[]}",
        "created_time":"1718334045",
        "expires_time_plus":"1718420447030",
        "attachments":[
            
        ],
        "thid":"635e1e59-54b2-4cce-b635-f70f6a4b0268",
        "ack":[
            
        ]
    },
    {
        "id":"99383a8d-6943-40ad-87dd-b224dff3c80c",
        "piuri":"https://didcomm.org/issue-credential/3.0/offer-credential",
        "from":{"method":"peer","methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},
        "to":{"method":"peer","methodId":"2.Ez6LSrhfy5nfumryQUhCU9CRFZvEy3zZV2pUedwpUoYbeiZbq.Vz6MksfNehMay3PPDQEfaaeCotpgC5z9hCnbF6s8uYWUGjSYh.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},
        "fromPrior":"null",
        "body":"{\"multiple_available\":null,\"goal_code\":\"Offer Credential\",\"credential_preview\":{\"schema_id\":\"http://host.docker.internal:8000/prism-agent/schema-registry/schemas/f52ebc61-6146-3238-99c5-a3fd693b96f7/schema\",\"type\":\"https://didcomm.org/issue-credential/3.0/credential-credential\",\"body\":{\"attributes\":[{\"media_type\":null,\"name\":\"emailaddress\",\"value\":\"cristian.castro@iohk.io\"}]}},\"replacement_id\":null,\"comment\":null}",
        "created_time":"1718334097",
        "expires_time_plus":"1718420498750",
        "attachments":[
            {
                "id":"3235ce90-8625-4668-9262-19c03d710fc4",
                "data":{
                    "base64":"eyJzY2hlbWFfaWQiOiJodHRwOi8vaG9zdC5kb2NrZXIuaW50ZXJuYWw6ODAwMC9wcmlzbS1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy9mNTJlYmM2MS02MTQ2LTMyMzgtOTljNS1hM2ZkNjkzYjk2Zjcvc2NoZW1hIiwiY3JlZF9kZWZfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9wcmlzbS1hZ2VudC9jcmVkZW50aWFsLWRlZmluaXRpb24tcmVnaXN0cnkvZGVmaW5pdGlvbnMvM2Y0ZWEwOTgtODdiNi0zMDJhLWE3MjYtZjkzZmU5MzhjZjI0L2RlZmluaXRpb24iLCJrZXlfY29ycmVjdG5lc3NfcHJvb2YiOnsiYyI6IjMwMDUwNzUwNjk4MDg5NzU1NDU4NDI4Njk1MDI2NTYxODQxOTAwMDEyMzUzNDQxMzY0NTQ5NTA3ODA1ODk5MzE1NDc0MDE1NzQ3NTcwIiwieHpfY2FwIjoiNTI1Njg5NDA1MjQ0MTg0ODQyODY5ODA1NDcxODY0ODkzODMwMDkxNTAwMDQyMjYwNDUxNzQ5NjQzMjExOTc0Nzc0Mzk5ODc0NDUwMjcyMzgxNjA4MjMzNzczODE5MjQ1OTIxNjA5MzY1OTk5MjEzNjQxOTc2MjU2ODk3OTM0NDY2MDA4NjY1NDY3ODU4NDQ2NzgwMTkyNDU1MzA2NTc5Njc5NTk1MDEwNTk0NTE0MTQ0MjkzNTQ2NzA2NTkzMTAxMDQ5MTY2NjM4MDcxMTcyMjY1Nzk3NzQ1MTQ1MzA5MTcwODUzNzI2MjY4OTg0NDc5OTQ2NjU0Nzc5NDI3NDg2NjQ4MjU2ODMwODgyMDM1MDU5ODExMjk4MjI5NDEwODYwMDA5MjI1NjUxODU3MjA5ODQyNzEzNDYxNDc1NzYxMTgyMzkyMDE5OTE2NTczNDk3NTM1ODYwNzM2NTI1NTcwOTg1NzMxMjY1Nzc4MjM3MjYyNTg1Mzg2MDM3ODI3NzkzMDQ3NjYzMTk5MjkwNjg4MTQ4NDEwMzcwMjE4MDY5NDAwOTE5Njc0ODU5NzQxMzY0OTQ0MzM1MjUyMDQ5MTUzMDI5NjI5MDc2NDE5OTU0MDY5NzIxMTMxNDU4ODU5NjcyNzc3NjUwOTYzNzY1ODIzNTE5OTY5Mzk0ODAyMzAwNDgwMjI4MzU4ODM1MzY4ODY1NzgxMjAzNjE0NjM5NjkyNjU3NjY3MTE2NjA1NTE0Njg4MjU2NDQwMjUxNzE2MjI1ODUwMDI1NTA0MjAxMzI1NTQyMDY0MzMzMTUxNjg5MTg1Mjc2NzQ3NzIyNzU1NjU4NTgxMzc4NjY0OTM2NTU3Mzk3NTg2MjQ1NzIzNDQ1NTE3NTgyMjM1NDY2MDk5NjYxOTEzNDI5MjA0ODMwMjQ0MTg5ODcxIiwieHJfY2FwIjpbWyJlbWFpbGFkZHJlc3MiLCI3NTExNTAzNTA1NTIyMTA5NzcyNDAyODA3OTQ5OTUxMTMxMTMxNjQ5NDE4NDY4NDExMzUxODgxOTA2Mzg2ODA0NTE1NjMyNjA1NDI0NzA0MTA0OTAxOTk1NjgxOTMwMDI5NTc2MTQ3NjkzNzc2MzY4NTgwMDUzMDE2MTUzODI1NjU5MDk3MjAwMjQyNDU3MDU0MjM3NjYyOTM5MDIyOTM5MTcyNzM5OTY5MTc1NTE5NjY5NzAwNzA2MzMzNjY5ODQxMDA1NTkyNzQxNjEwNDMxNDY4MDM2ODI3ODE0NTg0OTU3NDY5MTkzMTUxNDYyNzM0NTE5MzMwNzUyNzM3MzA4ODY5NzQwODMyMTM5NjQ2MzI0NDQwOTE4NDMyNTAyMzQ1MTg0ODYxODE5MzQ4OTMwOTg2NjAzMzYxMTE3NDIwNDY0NTgwNDIwNDA1OTA1MDI5MzExMjM0OTcyMzQyNzQ4MDI2NTk1NDMzMDcyOTk3NzUxMTEzNzc2NTI0ODMzNTU4Mjg3NTYwODg1ODc0MjY4ODQzMTA1OTY1MjM0MTg5ODg0NDc1MDU3ODM5MDg1NjkwMzcwMzU2NzczOTA0MDY2NTk2MjgzNTY2NTIxMjEzNjUxMTI3OTcwOTMyODkxMTU3MTAzMjM3NTE5NDMzMzgzOTg3MDU1MTg1MDUwMTE1MjQ2NTI3MDkzMTE4MTUyOTc0OTgwMDkyNTAxNDM1OTAwMTc2NDgxNzYzODc3NjI2OTU0ODU3ODQ2NzU4MTk1MTM1MTk2MDI3NjAzNzY2Mjc5MTM5MzEyNzY5MTI0MTE2ODA0Nzk2OTc3MTYwNDY2MTgwMDMyODE2MTI0ODA2MzYwMTYyNDI0Mzg0OTYwMzkyNjg5MjgzMTQ2NzQ4NzM5NDA5NTA5MDU2NTEyMDcyMjU5Mzk5MjEiXSxbIm1hc3Rlcl9zZWNyZXQiLCIxMTYxNDE3NjMxMjc2NTg2MzgzMTE0MjA4OTY5NzY2NjkxNjk4MTIxNDE4MTAxMDAzMjk5NjUxNDIzMzkyMDk0NzgwMTYwMTQ2MTQ3MDgzMTUzOTUzMjcxMDU4MTk3NDk5MDkyOTQ5NDIyNjA4OTgxODE4Mjg1MzMwOTQ2NTg1MjcwMjk1MjkyMTIwNjM0MTMwODIyNTMwNzQyNjkyODIxMTI5MzI5ODQ4NDU5NDY1NzE1Nzc2MjE3MjAyODA5NDEwNDgwMDg0MjEwMDU4OTU4NDAxNDQxNjMyNjU0NDc1OTU4NTgwNzU3MTIyNDc3NjczMTYxNzgxMjc2Mzc3NjA2ODg5NDk5NDg0NDA1NDY0ODU3NTUwNjQwNDcxNjMyNjIzNzMyMzUxNjk2Njg0Mzk5NzE3NjIwNDQ3NDUzMDUzMzE0NzgyODU2Nzc3MTc3MzI2Njk1MDk0ODM4ODUyODIyMjEyMTMxMTAxODgyMzM4NjQ1ODgyMDg2MTA3NjAyOTA5OTE2NTQ5MDQwOTk3NDE1NTk1OTU4Mjk0ODgzODg1ODQxNTUyMDcyMDI3NDA2NTc2MjUwMzAzNDMwMDMzODM4OTQ4NzgyMzA2MzY2NzAyMjUyMDcxMzE4NTE5NjQ4OTk5NzQ5ODQwODYzOTcyOTY5MDU1NTE3ODI4NDUzMDQ4MDkyMDU1OTQ3NDI2Mjk3MzI1NjIxMTE3OTU0NzE1MTUxMzYzNjUwODc4OTM1NTMzNTM2NzMzNjY1OTExMjAzNzkwMDY1NDg4MjMyNzUyMTkwMjU4NTA3MDMzMDYwMjU4NzgwMTU1MTA2NTQ2OTM5Mzc0ODI3MjQyODE3MjQ5NjUwMzE4OTY2MzkyNTYwNjEwMjMyMzg5NTUyMzU0NDI2ODU3OTA5MDI3NzU1Nzg1NTczMzA4MTEiXV19LCJub25jZSI6IjMzNTY5Njg3NzUyNDI0NzQzNzc2NTU0MCJ9"
                },
                "format":"anoncreds/credential-offer@v1.0"
            }
        ],
        "thid":"177d90ac-f7ac-41b1-af88-2dd8106633cb",
        "ack":[
            
        ]
    },
    {
        "id":"b6b4786a-8568-4ef4-9aa6-91e3657748e4",
        "piuri":"https://didcomm.org/issue-credential/3.0/request-credential",
        "from":{"method":"peer","methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},
        "to":{"method":"peer","methodId":"2.Ez6LSrhfy5nfumryQUhCU9CRFZvEy3zZV2pUedwpUoYbeiZbq.Vz6MksfNehMay3PPDQEfaaeCotpgC5z9hCnbF6s8uYWUGjSYh.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},
        "fromPrior":null,
        "body":"{\"goalCode\":\"Offer Credential\"}",
        "created_time":"1718334102032",
        "expires_time_plus":"1718420502032",
        "attachments":[
            {
                "id":"a6ebb2df-326e-4bc6-9e62-3d668dabea3b",
                "media_type":"application/json",
                "data":{
                    "base64":"eyJlbnRyb3B5IjoiZGlkOnByaXNtOjZmMjNkZGFjZTUxOWI2OGRmYzBmYTA2ZTk5MmRiNDBmMmYzYzU4NGFmMzgyY2U0NDZmYTJmZDBlMDQyZTVkZWE6Q29VQkNvSUJFanNLQjIxaGMzUmxjakFRQVVvdUNnbHpaV053TWpVMmF6RVNJUU1jS3dFaXRHYlFLdEdhLWpGWGkzbTF1N09QMkpNdWtZWFFuWmszZlFJWHZ4SkRDZzloZFhSb1pXNTBhV05oZEdsdmJqQVFCRW91Q2dselpXTndNalUyYXpFU0lRTWNLd0VpdEdiUUt0R2EtakZYaTNtMXU3T1AySk11a1lYUW5aazNmUUlYdnciLCJjcmVkX2RlZl9pZCI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL3ByaXNtLWFnZW50L2NyZWRlbnRpYWwtZGVmaW5pdGlvbi1yZWdpc3RyeS9kZWZpbml0aW9ucy8zZjRlYTA5OC04N2I2LTMwMmEtYTcyNi1mOTNmZTkzOGNmMjQvZGVmaW5pdGlvbiIsImJsaW5kZWRfbXMiOnsidSI6IjgwOTQ1NTUxODIxMzU3OTIyMTY0MTY5Njk5MDk2NDM0NzM4MzA2ODI3NDEyNzYxNzU1NDY3OTE5MjkyODQyNDAzNjMzOTAwMjIzNzM4NjE4MjE5NjQzMjYxNzAzMTMxMzg2MDc5NTI0NTI0ODA1NzQxMjE3NDA2MzAzNDE1ODQ4Njk5OTI3MjYzNTYxNjQ0NDQxODgzNjI3NDA4ODQzMDI3NzUwOTgxNjQxNTQ2NTk4NDE1ODY4MTY1MzAxNjYxNjQ1Nzc5NjE2ODg4OTY1Njg5NDIyMzc0MDcxMjA0Njg2NjY2MDI3NzIyNTUzNzgyOTU1ODMzMzAyODM0MjE3ODczNDI2NDI1NzM5MDY3NDU3MTIzNDcwNzg4MjMzNTkwMTY1NDIzNzQxMzM0NzA0NzU2ODM5OTE0OTczNjMzMjAxODgwNzQ4ODMyNDkxNjkyMTczOTQxMDM0NzQyOTk0MDA5OTk1NzQzNzU2NTY0ODgwODk2ODQxNjcyODE1OTA1ODM1MTkxMDg3MTA1ODU5OTk0MzUyOTA1OTk5NjA5NzczMzk3ODY1NDc4NDE2NjM5NzI2Mzk2ODE4MzM5NzM0ODcyODI1MDk2NzAxNDc4OTc5NzM3NzUwNjkyOTczOTAzNjgwMTcyMTM2ODE1NjE2ODQ5MzQ5NDk2NzM1MDI2OTQyOTI5NDUwNDIwNjkxMDUwMjQzMzg1OTcxMjE3NDQwMjY2NTYwMTU0NDkwOTc0Mjg2MTQxMjA2ODI4NzEzMDk3OTIwNjIyNTAzMTQ3NDM3MjI5Mzg0NzAyMjkzNjMxMTg0MDY0Mzg2MzE4IiwidXIiOm51bGwsImhpZGRlbl9hdHRyaWJ1dGVzIjpbIm1hc3Rlcl9zZWNyZXQiXSwiY29tbWl0dGVkX2F0dHJpYnV0ZXMiOnt9fSwiYmxpbmRlZF9tc19jb3JyZWN0bmVzc19wcm9vZiI6eyJjIjoiMzgxMTAzNDU1ODE3NjY5MjI3Mzg4OTE3MjAxMzQwNDc1MjUyNTUyOTA4NDQ0Mzg1NTQyNzQzMTAzMDE1NDAyMTI0MjUyOTg0NzcyODkiLCJ2X2Rhc2hfY2FwIjoiMjQ2MjAwMDk2OTkyMjcwNzYzMTgzOTMzMTEzMzExMDY3NzI0NjAwODgwMzE2MjM2NDQ0OTI5NDE4MjMyNTgxODU0ODA2NDk1ODI2MTYyMTE5MDk5MzkxNDEyODU5NTE2NTE3NjUwMTk5ODA4ODM2NzA4MzE0NTIzNTY2Mzk4MjkwNjI4ODkzNzQ0MDExODUzMDI0NDU4NDc1OTQ1OTI4ODYxODA2NjA4NjUwNTYyMzU3NDM1Nzc4NDUyNTQwOTY2OTM3Njk4MjY1MjQ5NjcxNjc0NjAwMzYxNTMyNDU4NjQ2MDcxOTYxNTAzNjE4NjMyMDczMzA3NzExMjc2MzcyMjAyNzQxNTgxNzA5MDcwNjQxNzU5OTAzNzk1MDA2NDYwMDI3NjAxMjI4NjAzNjM0MDkxNDcwMDkwNzEzMzUyODQyMjU3NDgzNDA1OTE4NDY5NTM1NTgxNzMyODgwMjQyNDk2NTEzOTAzMjM2MjYxMzcxMjYyNjM1NDI0NjE2Mjk3NTI2MzU5MjI1MzM2MzYzOTQ1NzQ0NTI5Mjg2NDQ4ODI2NTk3MDg0NzYxMDQwNTE5NDI1MzgyMzg2ODQ2MjM1NDU5NzI0MzMzMzc3ODEwNzcwNDc0MzMyMzIyMjk2MDc1MDE3ODQ0OTUwMDc5ODkxMzUwODU5ODEzMDc3MTkxMzQ1MTUxODU5MTcwMDk3Mzg5NzcxODQ2MTk4ODE0MjAzOTc0NjEzNDI3MDI3NzY1MzYyNDk4MjMwNjA0MzczNzgzNDExMzg1OTQyNzQ1MTQ3NzM5NzY5MTMxNzAyNTI4NzM2MTg2NjY1NTc0NTQ2MjkzNjQwOTYwOTkyOTYwMjQ0ODIyOTI2MTgzOTg4NTg2NjY1ODU3OTIwOTQ0MTQ0ODQxMTU4MjA0ODY0OTg1MTc1NDc1NjQ3MTc0Nzc3ODQ3NTk3Nzc3NjExNTUyOTQ5IiwibV9jYXBzIjp7Im1hc3Rlcl9zZWNyZXQiOiIxMzU5MDY0NjM1NDk5NjEwOTUyNTg2NzAyMDgzMjQzMDY2MDEwNjA1MjY3MDQyNjI4ODUwOTE0OTIxNzAxNDE0Mzg2MzYxMjQzMDg0NDg0ODEyNzA5NTUyMjY5NTA4NTg1ODg4MDI1OTUzODUzMDA4NDc4OTcwODg0OTMzMzE5ODkyOTI4NjA1MTcxNTY5NDU0OTE3NDE5NTAyMDI3NjczODAzODUzOTE0ODg0NTQ1MjQ4NCJ9LCJyX2NhcHMiOnt9fSwibm9uY2UiOiI5NTgwMjEwNzc4NDA2MzYxNDUwNDk2ODEifQ"
                },
                "format":"anoncreds/credential-request@v1.0"
            }
        ],
        "thid":"177d90ac-f7ac-41b1-af88-2dd8106633cb",
        "ack":[
            
        ],
        "direction":"SENT"
    },
    {
        "id":"5d2e5e11-4b6a-424b-921b-e89c3df596d7",
        "piuri":"https://didcomm.org/issue-credential/3.0/issue-credential",
        "from":{"method":"peer","methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},
        "to":{"method":"peer","methodId":"2.Ez6LSrhfy5nfumryQUhCU9CRFZvEy3zZV2pUedwpUoYbeiZbq.Vz6MksfNehMay3PPDQEfaaeCotpgC5z9hCnbF6s8uYWUGjSYh.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},
        "fromPrior":"null",
        "body":"{\"goal_code\":\"Issue Credential\",\"replacement_id\":null,\"more_available\":null,\"comment\":null}",
        "created_time":"1718334107",
        "expires_time_plus":"1718420509304",
        "attachments":[
            {
                "id":"a62c5510-37b7-4593-81eb-9190d9e14e3a",
                "data":{
                    "base64":"eyJzY2hlbWFfaWQiOiJodHRwOi8vaG9zdC5kb2NrZXIuaW50ZXJuYWw6ODAwMC9wcmlzbS1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy9mNTJlYmM2MS02MTQ2LTMyMzgtOTljNS1hM2ZkNjkzYjk2Zjcvc2NoZW1hIiwiY3JlZF9kZWZfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9wcmlzbS1hZ2VudC9jcmVkZW50aWFsLWRlZmluaXRpb24tcmVnaXN0cnkvZGVmaW5pdGlvbnMvM2Y0ZWEwOTgtODdiNi0zMDJhLWE3MjYtZjkzZmU5MzhjZjI0L2RlZmluaXRpb24iLCJyZXZfcmVnX2lkIjpudWxsLCJ2YWx1ZXMiOnsiZW1haWxhZGRyZXNzIjp7InJhdyI6ImNyaXN0aWFuLmNhc3Ryb0Bpb2hrLmlvIiwiZW5jb2RlZCI6IjM2MzQyMzgzNzQ1NTM4Njg0NzkxOTMwNTAzMzYyNTczOTQ0MjQzNzA2MjA3NTE4NzU5MDI1MTY5MTAyODM3MDAxMzM0NjExMzcyNzQzIn19LCJzaWduYXR1cmUiOnsicF9jcmVkZW50aWFsIjp7Im1fMiI6IjEwODg5NjAwNjg2NTY2NzkwOTA2Njc3NTU5ODIxNjY4NjU5NjUyNDE3MzYxNjcwNTYzMzgzNDMwMDEzMDYxNDU4NDMzMzIyNTUxODIwMyIsImEiOiIzNDIxODA5NzM3MzQ2MTYyNDQ5NzIzOTI1NDYxNTg4ODY1NTYzMDg1NTIyNDY0OTY0MDI4NzQwMjAxNzUzNTQ4ODcyNzQ4NDUyMzg4OTM3MDM2Mjk1MzIxOTg4OTU1OTgwMjg4OTMyNTcyNzc4NTgyNDcwODIzNDc3MjQ5NTcwMzA3NTkyNjU3ODQ0MjAyMDY5MDI5Nzk5NjQ2NzI3Mjk2OTQ5OTg4NDMzMDMzMzAzNDUzMDI3NTkzMjc5ODYyMzAwNTg2MzA4MjgyNTI4MjMzODQ5MTAyOTAyODE2MzA1MTEwODE5NTU2MDEyNzU0NDUxMjAzNTAzODUyMDUyMjczMzg1NjIyMTU2NDk5MTYxNzg1Mjc1MDcxNjQ2ODYyNTcwNTk3Nzg1MDU0NDcyNTY2ODUyODc1NjA3NjUzMzQxMDQwODM2Njg5MzAxMjEyOTc3NDkyMjA1MTk0Mjg0MzEzODQyNzMyMzM4NTcwMTgwNjAzMDEyOTA0ODAwMDQxNDIzMTg1ODQ2ODgzNjM2Mjk0MDI0MDczNTQ5MDgzMDg4NTMzOTQ3NDUzMTM1MTkyNzYyODYzNzg3NDE2NTYxNzEyNDczNTE4OTcyNTU2ODYzNjQ4NTYwMjI2NDE2Nzk1MjgyODQ1OTk3MjU4ODkyMjE2OTA2MDcxNzUwODM5NTgwNTg4MzYzMTAxMDY5NjA4MzA3MDIzNzc2NDU5Mzg1MjA2NDQ3ODcxMjU3MzY3MTI4MDIyNzAxODM2MzI2MTEzNTcxMDAyOTc3Njk1MzgwODE2MDE5MzgxOTQxNDQ4MDk0MDU3ODYwNDMwIiwiZSI6IjI1OTM0NDcyMzA1NTA2MjA1OTkwNzAyNTQ5MTQ4MDY5NzU3MTkzODI3Nzg4OTUxNTE1MjMwNjI0OTcyODU4MzEwNTY2NTgwMDcxMzMwNjc1OTE0OTk4MTY5MDU1OTE5Mzk4NzE0MzAxMjM2NzkxMzIwNjI5OTMyMzg5OTY5Njk0MjIxMzIzNTk1Njc0MjkzMDE0MDYwMzc0NDMwNTk3MDc5NzA2NTE4MjI5MjY3MjY2NjI5OSIsInYiOiI5Mzk3ODc5ODYxNTE4MTM5MDkyMTczOTQ0NTk1NTYxNTMzMjM4ODc0MTU2MTEzMTMzMDA2MDg2MDg0Njc1NzM5MDYwODA3MzYxOTM0OTAyMzg1MTYyODM5Mjg5Mjc2NjkwMTYyMzIxOTg4OTA3Njg0Mzk3OTE1Mzk5MTU4OTgxOTAxMTE1ODg0MTE4NDc5NzY1ODQzNjI2MjQzNDg3NjUwMzY5NjY1Mzk2MDc1NDA2NTkwNzM2ODk0NTUzMjY4OTMwODA1NzIxNDYxNTMxMjE2NzM1NDA3MzI3MDQ2MTAxMDk3NjY1OTMwMjI3MDY4Njk4MDQyNTUzNzYwNjM2MDI5NjU3ODMzODQzMDkyMTQ1MDkyNDUzODM3MDAwMTgzNjk0NDgyNzkwODk2MzA2ODE0MTYyNTIxMDE5NDA2NDg0NzEzODY2ODA2NDc1MDIxMTIwOTg2NTg1MDU1NTAyODg5MDY2NjIxNjQ1NzI3ODUyODI1NDA5NjAwNzkwNzA0MDM2NjEzMTQ1ODIzNjU3NzU4NzM4NTA1NDQyOTkwMTk0NjM1Mjk3NDI3Mzk1NzQ2MjM5MzkxMjg3NzUxODU4NjY1NzU3OTI3MjkyOTQyODgxMzgzNTQ1NDU4NzI1NTQ2NDkyODUxNjU2NzM4MjI3MDMyMjcxMDY3MDMzMjAxNjU1Mzk4MzU4MTQ2NTkwNjE2ODA0ODU0MDA5NzIwMTEzOTgwODYxMzMyOTQ3OTU0NzIyMTYzODI1MjAzMjAzMzc5ODMzMzc4OTU0NTk1OTQ1MzMwOTUxNjI4ODE0NTIwNjM2Nzc5MDM5NjQwMzg2ODY4NTAxOTQzOTU5OTkwNDY3MTMzNDQ4NzA2NjM5MzMxMTgwNTQwNzEzODg2NTA1MzEzODc5MDI2NjkyMTIyODc0NzcxOTMxMjY0NDE0OTM1NDEwNTI0NDg1Mjk2NjgyNzI1ODU4OTUyMTQzNzI4ODgyNjMyNjA4NTA0ODAxNjYyMTU0NTUzMjc2NDExMzE4NzU0ODEzNjc3MTU2MTc3MDQ5ODI2MTcyNDUyNzAxMTMxNDM2MDYxMDMyNzMxMTMyMjYxNjQwNDIxIn0sInJfY3JlZGVudGlhbCI6bnVsbH0sInNpZ25hdHVyZV9jb3JyZWN0bmVzc19wcm9vZiI6eyJzZSI6IjEyNzcyNTkxMjEwOTAwMDU0MTE1Njc0MjA1OTA4MTI0NDkzMTUzODc4MTg4NTY2NjkyMTkxMDM1MTU3MTA0Njk3OTk4MjA0NzY5MjExMzA4NDg3ODU5MzM2NTYwMTYwMTU2MjcyMTE3NTk1ODg3NzUzODk0NzQxNjc1OTIzMTQ5MjU1NzgyODI5OTEzNjk4MzQ5MTAyNjg0MDcwNzE2Nzc3NTAyMDY0NjgwMDMyMDEwMzc2NTUwODM0NDU4OTM1NjA2MzU2MTQzODAxNDkwNTQ0MTExODgyMzA2ODUwMDU0MDU4ODA1MTc2OTM2MTY0OTQxOTk0MDY0NDUyMTY5MjAwMDY3OTU3NDkwNDg2NDA5MzI1MTU2NDgxOTI0NTQwOTcxNDE0MTc2NjQ3NzQ0MzEzODkwMDU0NTAxMzg0NzI2NDU3NTc3MzMyNjQzMjgxNDUzNzIyNzA4ODI4MjEyMDk3ODg2MDIzNDg0NzU5MzM1NTc3NTgzOTMzMzM1NTk4MTI5NDgyNzIyNzE4MTAzOTQ5MTQzNDY1ODk1NTg2OTExMTQ3ODM5Mzk2OTUwODMyNTkyMzA3NTcxNTkyMTA4NTcwNjg4MjI1ODM1NjU5MTc4NzgyNDE2NTQxNTg2Njg2NTIzNzY3MDI3MTExNDIwNzA3MDQwNzYzNDczNTQ2MDIxMjgxMTkyODI3NDc2NTE5ODc5ODYyMDAwODM3OTY5NTA4OTUzMTE1MDk0MzE3OTA1NzE3NTAxNTE5OTMyODAzOTI4ODIyMTYxMjIwMDk1NzczMTAwMjg3NjIwNjQxMDA1ODcxNDQ1MzA2IiwiYyI6IjkzNzkzMjcxOTUyNzkxNjM3NDIyODYxMTQzODAyMzM2NzAzODk3MjkxNzI2MDUwNTAyNDQwNTk5MjcwNDA2MzUyMDg5ODM2MDI5OTE5In0sInJldl9yZWciOm51bGwsIndpdG5lc3MiOm51bGx9"
                },
                "format":"anoncreds/credential@v1.0"
            }
        ],
        "thid":"177d90ac-f7ac-41b1-af88-2dd8106633cb",
        "ack":[
            
        ]
    },
    {
        "id":"cf1734d1-51d5-4275-9a40-18c36f44fb03",
        "piuri":"https://didcomm.org/issue-credential/3.0/offer-credential",
        "from":{"method":"peer","methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},
        "to":{"method":"peer","methodId":"2.Ez6LSrhfy5nfumryQUhCU9CRFZvEy3zZV2pUedwpUoYbeiZbq.Vz6MksfNehMay3PPDQEfaaeCotpgC5z9hCnbF6s8uYWUGjSYh.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},
        "fromPrior":"null",
        "body":"{\"multiple_available\":null,\"goal_code\":\"Offer Credential\",\"credential_preview\":{\"schema_id\":null,\"type\":\"https://didcomm.org/issue-credential/3.0/credential-credential\",\"body\":{\"attributes\":[{\"media_type\":null,\"name\":\"familyName\",\"value\":\"demo\"},{\"media_type\":null,\"name\":\"drivingClass\",\"value\":\"1\"},{\"media_type\":null,\"name\":\"dateOfIssuance\",\"value\":\"01/01/2024\"},{\"media_type\":null,\"name\":\"emailAddress\",\"value\":\"demo@email.com\"},{\"media_type\":null,\"name\":\"drivingLicenseID\",\"value\":\"A1221332\"}]}},\"replacement_id\":null,\"comment\":null}",
        "created_time":"1718334142",
        "expires_time_plus":"1718420545527",
        "attachments":[
            {
                "id":"6ba0515b-0f68-4c0a-8ecb-487d01dec978",
                "data":{
                    "data":"{\"options\":{\"domain\":\"domain\",\"challenge\":\"fd8c7f98-8473-42ba-83a8-5cfa460c55b9\"},\"presentation_definition\":{\"purpose\":null,\"format\":{\"jwt\":{\"proof_type\":[],\"alg\":[\"ES256K\"]},\"ldp\":null},\"name\":null,\"input_descriptors\":[],\"id\":\"3cb224c2-cb61-4f5d-ae5f-2fa89572597d\"}}"
                },
                "format":"prism/jwt"
            }
        ],
        "thid":"7524fc50-d834-4a1f-a617-f36dd1d571bd",
        "ack":[
            
        ]
    },
    {
        "id":"b3ec0d9c-c28b-4489-8b2d-3c936c32c665",
        "piuri":"https://didcomm.org/issue-credential/3.0/request-credential",
        "from":{"method":"peer","methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},
        "to":{"method":"peer","methodId":"2.Ez6LSrhfy5nfumryQUhCU9CRFZvEy3zZV2pUedwpUoYbeiZbq.Vz6MksfNehMay3PPDQEfaaeCotpgC5z9hCnbF6s8uYWUGjSYh.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},
        "fromPrior":null,
        "body":"{\"goalCode\":\"Offer Credential\"}",
        "created_time":"1718334149224",
        "expires_time_plus":"1718420549224",
        "attachments":[
            {
                "id":"02ae8939-6a48-4828-bbf9-10a68c0c4006",
                "media_type":"prism/jwt",
                "data":{
                    "base64":"ZXlKaGJHY2lPaUpGVXpJMU5rc2lmUS5leUpwYzNNaU9pSmthV1E2Y0hKcGMyMDZNR0UwWWpVMU1qRTJPV1V6TVRVNE56Z3hOelF4Wm1KaVpXWm1aVGd4TWpFeU56ZzBaRE15WkRrd1kyWTRaakkyTWpJNU1qTm1NVEZtTm1WalpEazJOanBEYjFWQ1EyOUpRa1ZxYzB0Q01qRm9Zek5TYkdOcVFWRkJWVzkxUTJkc2VscFhUbmROYWxVeVlYcEZVMGxSVEdkNmFITjFUM0ZvUVhsSmJYa3RZemh2T1ZwdFNVbzBhVmxmUjJNNGRIWk9TVlF6YkRGM05UaG1Na0pLUkVObk9XaGtXRkp2V2xjMU1HRlhUbWhrUjJ4MlltcEJVVUpGYjNWRFoyeDZXbGRPZDAxcVZUSmhla1ZUU1ZGTVozcG9jM1ZQY1doQmVVbHRlUzFqT0c4NVdtMUpTalJwV1Y5SFl6aDBkazVKVkROc01YYzFPR1l5UVNJc0ltRjFaQ0k2SW1SdmJXRnBiaUlzSW5ad0lqcDdJa0JqYjI1MFpYaDBJanBiSW1oMGRIQnpPbHd2WEM5M2QzY3Vkek11YjNKblhDOHlNREU0WEM5amNtVmtaVzUwYVdGc2Mxd3ZkakVpWFN3aWRIbHdaU0k2V3lKV1pYSnBabWxoWW14bFVISmxjMlZ1ZEdGMGFXOXVJbDE5TENKdWIyNWpaU0k2SW1aa09HTTNaams0TFRnME56TXROREppWVMwNE0yRTRMVFZqWm1FME5qQmpOVFZpT1NKOS5mejg3SFNSYU5xb0d0Y3lXXzNkd2JRanE0ckR3LXlTME9ST1dnQ1I5LTBlM0RMTk4wM1p6UHVzejkzQnMza08wR2JrU3NaN1VLWGliZlBqNlpoQ3lPUQ"
                }
            }
        ],
        "thid":"7524fc50-d834-4a1f-a617-f36dd1d571bd",
        "ack":[
            
        ],
        "direction":"SENT"
    },
    {
        "id":"e21457cc-d550-40f5-80ac-0e9d265adb47",
        "piuri":"https://didcomm.org/issue-credential/3.0/issue-credential",
        "from":{"method":"peer","methodId":"2.Ez6LSqWfJdwLMDmpew7Yd8AQS2MxDwvSCNpjaZ7EQVst8rkfA.Vz6MknpCj4WomXhC2eur8nf4wnoFzHpCA6EAFU6afEmmrUVRA.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},
        "to":{"method":"peer","methodId":"2.Ez6LSrhfy5nfumryQUhCU9CRFZvEy3zZV2pUedwpUoYbeiZbq.Vz6MksfNehMay3PPDQEfaaeCotpgC5z9hCnbF6s8uYWUGjSYh.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},
        "fromPrior":"null",
        "body":"{\"goal_code\":null,\"replacement_id\":null,\"more_available\":null,\"comment\":null}",
        "created_time":"1718334153",
        "expires_time_plus":"1718420555892",
        "attachments":[
            {
                "id":"69856638-0bd4-4d4c-be10-5b4c2fb56b03",
                "data":{
                    "base64":"ZXlKaGJHY2lPaUpGVXpJMU5rc2lmUS5leUpwYzNNaU9pSmthV1E2Y0hKcGMyMDZNek0zWlRobVpURTBOR0ZoWTJWa00yTmhNMlJrTlRrME5qSTBNRFJtTkRVNU9UWmxNMkl5TWpGaFltTTBNVEJoTnpJMVpXRTJOalV6TkRnNU56SmlZanBEY210Q1EzSlpRa1ZxYjB0Q2JVWXhaRWRuZEUxU1FVVlRhVFJMUTFoT2JGa3pRWGxPVkZweVRWSkphRUYyZVdjeFlUTjFjSFZtYkZCTGN6aEtSMWhLVTNOeFYxcGpWRzlHUVhrM1JqTlNURkJqUWxrMFYyNXpSV3B6UzBJeWJIcGpNMVpzVEZSRlVVRnJiM1ZEWjJ4NldsZE9kMDFxVlRKaGVrVlRTVkZQWVZCVWJ6TTVUbmgyVW1oWFVXNWlWV2hvVFhNNWJURkllRUp0Y1Y5aFpXTkhNMHRUVEdaaU5XZ3pVa2szUTJka2RGbFlUakJhV0VsM1JVRkdTMHhuYjBwak1sWnFZMFJKTVU1dGMzaEZhVVZFWjNwb09FbERZMVpoTlZsTlpqWXpSRkZhTTE5MWRUTk9Nek5zU1hWR1NHSm9YMDlLVWxWSWJXZDJZeUlzSW5OMVlpSTZJbVJwWkRwd2NtbHpiVG93WVRSaU5UVXlNVFk1WlRNeE5UZzNPREUzTkRGbVltSmxabVpsT0RFeU1USTNPRFJrTXpKa09UQmpaamhtTWpZeU1qa3lNMll4TVdZMlpXTmtPVFkyT2tOdlZVSkRiMGxDUldwelMwSXlNV2hqTTFKc1kycEJVVUZWYjNWRFoyeDZXbGRPZDAxcVZUSmhla1ZUU1ZGTVozcG9jM1ZQY1doQmVVbHRlUzFqT0c4NVdtMUpTalJwV1Y5SFl6aDBkazVKVkROc01YYzFPR1l5UWtwRVEyYzVhR1JZVW05YVZ6VXdZVmRPYUdSSGJIWmlha0ZSUWtWdmRVTm5iSHBhVjA1M1RXcFZNbUY2UlZOSlVVeG5lbWh6ZFU5eGFFRjVTVzE1TFdNNGJ6bGFiVWxLTkdsWlgwZGpPSFIyVGtsVU0yd3hkelU0WmpKQklpd2libUptSWpveE56RTRNek0wTVRVeExDSjJZeUk2ZXlKamNtVmtaVzUwYVdGc1UzVmlhbVZqZENJNmV5SmxiV0ZwYkVGa1pISmxjM01pT2lKa1pXMXZRR1Z0WVdsc0xtTnZiU0lzSW1SeWFYWnBibWREYkdGemN5STZJakVpTENKbVlXMXBiSGxPWVcxbElqb2laR1Z0YnlJc0ltUnlhWFpwYm1kTWFXTmxibk5sU1VRaU9pSkJNVEl5TVRNek1pSXNJbWxrSWpvaVpHbGtPbkJ5YVhOdE9qQmhOR0kxTlRJeE5qbGxNekUxT0RjNE1UYzBNV1ppWW1WbVptVTRNVEl4TWpjNE5HUXpNbVE1TUdObU9HWXlOakl5T1RJelpqRXhaalpsWTJRNU5qWTZRMjlWUWtOdlNVSkZhbk5MUWpJeGFHTXpVbXhqYWtGUlFWVnZkVU5uYkhwYVYwNTNUV3BWTW1GNlJWTkpVVXhuZW1oemRVOXhhRUY1U1cxNUxXTTRiemxhYlVsS05HbFpYMGRqT0hSMlRrbFVNMnd4ZHpVNFpqSkNTa1JEWnpsb1pGaFNiMXBYTlRCaFYwNW9aRWRzZG1KcVFWRkNSVzkxUTJkc2VscFhUbmROYWxVeVlYcEZVMGxSVEdkNmFITjFUM0ZvUVhsSmJYa3RZemh2T1ZwdFNVbzBhVmxmUjJNNGRIWk9TVlF6YkRGM05UaG1Na0VpTENKa1lYUmxUMlpKYzNOMVlXNWpaU0k2SWpBeFhDOHdNVnd2TWpBeU5DSjlMQ0owZVhCbElqcGJJbFpsY21sbWFXRmliR1ZEY21Wa1pXNTBhV0ZzSWwwc0lrQmpiMjUwWlhoMElqcGJJbWgwZEhCek9sd3ZYQzkzZDNjdWR6TXViM0puWEM4eU1ERTRYQzlqY21Wa1pXNTBhV0ZzYzF3dmRqRWlYU3dpWTNKbFpHVnVkR2xoYkZOMFlYUjFjeUk2ZXlKemRHRjBkWE5RZFhKd2IzTmxJam9pVW1WMmIyTmhkR2x2YmlJc0luTjBZWFIxYzB4cGMzUkpibVJsZUNJNk5Td2lhV1FpT2lKb2RIUndPbHd2WEM4eE9USXVNVFk0TGpZNExqRXhNem80TURBd1hDOXdjbWx6YlMxaFoyVnVkRnd2WTNKbFpHVnVkR2xoYkMxemRHRjBkWE5jTHpNNVlqQmlOekkyTFRCbU5tVXRORGxtTnkwNVl6VXlMVFl5WVRjNE1UY3hOelZsT0NNMUlpd2lkSGx3WlNJNklsTjBZWFIxYzB4cGMzUXlNREl4Ulc1MGNua2lMQ0p6ZEdGMGRYTk1hWE4wUTNKbFpHVnVkR2xoYkNJNkltaDBkSEE2WEM5Y0x6RTVNaTR4TmpndU5qZ3VNVEV6T2pnd01EQmNMM0J5YVhOdExXRm5aVzUwWEM5amNtVmtaVzUwYVdGc0xYTjBZWFIxYzF3dk16bGlNR0kzTWpZdE1HWTJaUzAwT1dZM0xUbGpOVEl0TmpKaE56Z3hOekUzTldVNEluMTlmUS5maW5ESHhybHRxbU9CcXBEZ3Zfa0NMVk02dnZCRFU3YWRoY01UV3Y2VTRwMlBha3puc0htbDl2TXpxNGpidWlfTXAwZDFoTm0tUXVVcFRMSUFiY2Z1QQ=="
                },
                "format":"prism/jwt"
            }
        ],
        "thid":"7524fc50-d834-4a1f-a617-f36dd1d571bd",
        "ack":[
            
        ]
    }
]
        """
    val getMediator = """
        [
            {
                "mediator_did":{"method":"peer","methodId":"2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHBzOi8vc2l0LXByaXNtLW1lZGlhdG9yLmF0YWxhcHJpc20uaW8iLCJhIjpbImRpZGNvbW0vdjIiXX19.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6IndzczovL3NpdC1wcmlzbS1tZWRpYXRvci5hdGFsYXByaXNtLmlvL3dzIiwiYSI6WyJkaWRjb21tL3YyIl19fQ"},
                "holder_did":{"method":"peer","methodId":"2.Ez6LSok96TA4orHQXSMHZj3mqyUuVLMfLfGGqj27i1giErbXL.Vz6Mku5mY1GuJ9AN2vvDwjMv5QUC2zqKVRPCcbmJVYTFTCFmr"},
                "routing_did":{"method":"peer","methodId":"2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHBzOi8vc2l0LXByaXNtLW1lZGlhdG9yLmF0YWxhcHJpc20uaW8iLCJhIjpbImRpZGNvbW0vdjIiXX19.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6IndzczovL3NpdC1wcmlzbS1tZWRpYXRvci5hdGFsYXByaXNtLmlvL3dzIiwiYSI6WyJkaWRjb21tL3YyIl19fQ"}
            }
        ]
        """
}
