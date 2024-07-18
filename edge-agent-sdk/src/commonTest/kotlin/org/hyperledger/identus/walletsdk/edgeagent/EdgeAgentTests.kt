@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.edgeagent

import anoncreds_wrapper.LinkSecret
import io.ktor.http.HttpStatusCode
import java.security.interfaces.ECPublicKey
import java.util.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.hyperledger.identus.apollo.base64.base64UrlDecoded
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.apollo.derivation.MnemonicHelper
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.castor.CastorImpl
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Mercury
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pollux
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentBase64
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.ClaimType
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDResolver
import org.hyperledger.identus.walletsdk.domain.models.DIDUrl
import org.hyperledger.identus.walletsdk.domain.models.HttpResponse
import org.hyperledger.identus.walletsdk.domain.models.KeyCurve
import org.hyperledger.identus.walletsdk.domain.models.Mediator
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.PresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.ProvableCredential
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.Signature
import org.hyperledger.identus.walletsdk.edgeagent.helpers.AgentOptions
import org.hyperledger.identus.walletsdk.edgeagent.mediation.MediationHandler
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.IssueCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.OfferCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.OutOfBandInvitation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.PrismOnboardingInvitation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationDefinitionRequest
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmission
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.RequestPresentation
import org.hyperledger.identus.walletsdk.logger.PrismLoggerMock
import org.hyperledger.identus.walletsdk.mercury.ApiMock
import org.hyperledger.identus.walletsdk.pluto.StorablePrivateKey
import org.hyperledger.identus.walletsdk.pollux.PolluxImpl
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.CredentialRequestMeta
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import java.security.interfaces.ECPublicKey
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EdgeAgentTests {

    @Mock
    lateinit var plutoMock: Pluto

    @Mock
    lateinit var polluxMock: Pollux

    @Mock
    lateinit var castorMock: Castor

    @Mock
    lateinit var connectionManagerMock: ConnectionManager

    @Mock
    lateinit var mediationHandlerMock: MediationHandler

    lateinit var apolloMock: ApolloMock
    lateinit var castorMockOld: CastorMock
    lateinit var plutoMockOld: PlutoMock
    lateinit var mercuryMock: MercuryMock
    lateinit var polluxMockOld: PolluxMock
    lateinit var mediationHandlerMockOld: MediationHandlerMock
    lateinit var connectionManagerOld: ConnectionManager
    lateinit var json: Json

    @BeforeTest
    fun setup() {
        MockitoAnnotations.openMocks(this)
        apolloMock = ApolloMock()
        castorMockOld = CastorMock()
        plutoMockOld = PlutoMock()
        mercuryMock = MercuryMock()
        polluxMockOld = PolluxMock()
        mediationHandlerMockOld = MediationHandlerMock()
        // Pairing will be removed in the future
        connectionManagerOld =
            ConnectionManagerImpl(
                mercuryMock,
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
            mercuryMock,
            castorMockOld,
            plutoMock,
            mediationHandlerMock,
            mutableListOf(),
            polluxMock
        )

        val agent = spy(
            EdgeAgent(
                apollo = apolloMock,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMock,
                pollux = polluxMockOld,
                connectionManager = connectionManager,
                seed = null,
                api = null,
                logger = PrismLoggerMock(),
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
            mercuryMock,
            castorMockOld,
            plutoMock,
            mediationHandlerMock,
            mutableListOf(),
            polluxMock
        )

        val agent = spy(
            EdgeAgent(
                apollo = apolloMock,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMock,
                pollux = polluxMockOld,
                connectionManager = connectionManager,
                seed = null,
                api = null,
                logger = PrismLoggerMock(),
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
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMock,
                pollux = polluxMockOld,
                connectionManager = connectionManagerMock,
                seed = null,
                api = null,
                logger = PrismLoggerMock(),
                agentOptions = AgentOptions()
            )
        )
        agent.stop()
        assertEquals(EdgeAgent.State.STOPPED, agent.state)
    }

    @Test
    fun `EdgeAgent setupMediatorHandler should stop the agent and replace the current mediatior handler`() = runTest {
        val connectionManager = ConnectionManagerImpl(
            mercuryMock,
            castorMockOld,
            plutoMock,
            mediationHandlerMock,
            mutableListOf(),
            polluxMock
        )
        val agent = spy(
            EdgeAgent(
                apollo = apolloMock,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMock,
                pollux = polluxMockOld,
                connectionManager = connectionManagerMock,
                seed = null,
                api = null,
                logger = PrismLoggerMock(),
                agentOptions = AgentOptions()
            )
        )
        val mediatorHandlerMock2 = mock<MediationHandler>()
        agent.setupMediatorHandler(mediatorHandlerMock2)

    }

    @Test
    fun testCreateNewPrismDID_shouldCreateNewDID_whenCalled() = runTest {
        val seed = Seed(MnemonicHelper.createRandomSeed())
        val validDID = DID("did", "test", "123")
        castorMockOld.createPrismDIDReturn = validDID
        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMock,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = seed,
            api = null,
            logger = PrismLoggerMock(),
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
        val validDID = DID("did", "test", "123")
        castorMockOld.createPeerDIDReturn = validDID
        val agent = EdgeAgent(
            apolloMock,
            castorMockOld,
            plutoMockOld,
            mercuryMock,
            polluxMockOld,
            connectionManagerOld,
            null,
            null,
            logger = PrismLoggerMock(),
            agentOptions = AgentOptions()
        )

        val newDID = agent.createNewPeerDID(services = emptyArray(), updateMediator = false)

        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMockOld.storedPeerDID.first())
        assertTrue { plutoMockOld.wasStorePeerDIDAndPrivateKeysCalled }
    }

    @Test
    fun testCreateNewPeerDID_whenUpdateMediatorFalse_thenShouldUseProvidedServices() = runTest {
        val apollo = ApolloImpl()
        val castor = CastorImpl(apollo = apollo, logger = PrismLoggerMock())
        val agent = EdgeAgent(
            apollo,
            castor,
            plutoMockOld,
            mercuryMock,
            polluxMockOld,
            connectionManagerOld,
            null,
            null,
            logger = PrismLoggerMock(),
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
                apollo = apolloMock,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMock,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
                logger = PrismLoggerMock()
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
                apollo = apolloMock,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMock,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = api,
                logger = PrismLoggerMock()
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
                apollo = apolloMock,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMock,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
                logger = PrismLoggerMock()
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
                apollo = apolloMock,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMock,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = null,
                logger = PrismLoggerMock()
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
    fun testPrismAgentSignWith_whenPrivateKeyAvailable_thenSignatureReturned() = runTest {
        val apolloMock = mock<Apollo>()
        val plutoMock = mock<Pluto>()
        val mnemonics = MnemonicHelper.createRandomMnemonics().toTypedArray()
        val seed = Seed(
            value = MnemonicHelper.createSeed(
                mnemonics = mnemonics.asList(),
                passphrase = ""
            )
        )
        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = seed,
            api = null,
            logger = PrismLoggerMock(),
            agentOptions = AgentOptions()
        )

        val privateKey = Secp256k1KeyPair.generateKeyPair(
            seed = Seed(MnemonicHelper.createRandomSeed()),
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
        `when`(apolloMock.restorePrivateKey(storablePrivateKeys.first())).thenReturn(privateKey)

        val did = DID("did", "peer", "asdf1234asdf1234")
        val messageString = "This is a message"

        assertEquals(Signature::class, agent.signWith(did, messageString.toByteArray())::class)
        verify(plutoMock).getDIDPrivateKeysByDID(any())
    }

    @Test
    fun testParseInvitation_whenOutOfBand_thenReturnsOutOfBandInvitationObject() = runTest {
        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMock,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = null,
            api = null,
            logger = PrismLoggerMock(),
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
                apollo = apolloMock,
                castor = castorMockOld,
                pluto = plutoMockOld,
                mercury = mercuryMock,
                pollux = polluxMockOld,
                connectionManager = connectionManagerOld,
                seed = null,
                api = null,
                logger = PrismLoggerMock()
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
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMock,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = null,
            api = null,
            logger = PrismLoggerMock(),
            agentOptions = AgentOptions()
        )
        assertEquals(EdgeAgent.State.STOPPED, agent.state)
        agent.start()
        assertEquals(EdgeAgent.State.RUNNING, agent.state)
    }

    @Test
    fun testStopPrismAgent_whenCalled_thenStatusIsStopped() = runTest {
        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMock,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = null,
            api = null,
            logger = PrismLoggerMock(),
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
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMock,
            pollux = polluxMockOld,
            connectionManager = connectionManagerOld,
            seed = null,
            api = null,
            logger = PrismLoggerMock(),
            agentOptions = AgentOptions()
        )
        val x = agent.parseInvitation(oob)
        assert(x is OutOfBandInvitation)
        assert((x as OutOfBandInvitation).type == ProtocolType.Didcomminvitation)
    }

    @AndroidIgnore
    @Test
    fun testPrepareRequestCredentialWithIssuer_whenAnoncredOfferCredential_thenProcessed() = runTest {
        val apiMock: Api = ApiMock(
            HttpStatusCode(200, "Ok"),
            getCredentialDefinitionResponse
        )
        val pollux = PolluxImpl(apolloMock, castorMockOld, apiMock)
        plutoMockOld.getLinkSecretReturn = flow { emit(LinkSecret().getValue()) }

        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMock,
            pollux = pollux,
            connectionManager = connectionManagerOld,
            seed = null,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = PrismLoggerMock(),
            agentOptions = AgentOptions()
        )

        val message = Json.decodeFromString<Message>(
            """{"id":"e430e4af-455e-4a15-9f2f-5bd8e5f350b8","piuri":"https://didcomm.org/issue-credential/3.0/offer-credential","from":{"method":"peer","methodId":"2.Ez6LSm5hETc4CS4X8RxYYKjoS2B3CM8TyzbgRrE7kGrdymHdq.Vz6MkoP2VXs4N7iNsKTzEKtZbnfu6yDH1x2ajGtCmNmc6qdMW.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHA6Ly8xOTIuMTY4LjY4LjExMzo4MDAwL2RpZGNvbW0iLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19fQ"},"to":{"method":"peer","methodId":"2.Ez6LSd8irQSWjjMfvg53kcaDY6Q2doPEvQwscjSzidgWoFUVK.Vz6Mksu4QVe8oKwJEDPgxRg2bFa3QWrZR1EZGC9xq8xk9twYX.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6ImRpZDpwZWVyOjIuRXo2TFNnaHdTRTQzN3duREUxcHQzWDZoVkRVUXpTanNIemlucFgzWEZ2TWpSQW03eS5WejZNa2hoMWU1Q0VZWXE2SkJVY1RaNkNwMnJhbkNXUnJ2N1lheDNMZTRONTlSNmRkLlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW1oMGRIQnpPaTh2YzJsMExYQnlhWE50TFcxbFpHbGhkRzl5TG1GMFlXeGhjSEpwYzIwdWFXOGlMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDE5LlNleUowSWpvaVpHMGlMQ0p6SWpwN0luVnlhU0k2SW5kemN6b3ZMM05wZEMxd2NtbHpiUzF0WldScFlYUnZjaTVoZEdGc1lYQnlhWE50TG1sdkwzZHpJaXdpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5ZlEiLCJyIjpbXSwiYSI6W119fQ"},"fromPrior":"null","body":"{\"multiple_available\":null,\"goal_code\":\"Offer Credential\",\"credential_preview\":{\"schema_id\":\"http:\\/\\/192.168.68.113:8000\\/cloud-agent\\/schema-registry\\/schemas\\/5667190d-640c-36af-a9f1-f4ed2587e766\\/schema\",\"type\":\"https:\\/\\/didcomm.org\\/issue-credential\\/3.0\\/credential-credential\",\"body\":{\"attributes\":[{\"media_type\":null,\"name\":\"age\",\"value\":\"18\"},{\"media_type\":null,\"name\":\"name\",\"value\":\"Cristian\"}]}},\"replacement_id\":null,\"comment\":null}","created_time":"1721242264","expires_time_plus":"1721328667","attachments":[{"id":"ee903fe0-2c49-4356-9b41-cfccc979c0a1","data":{"base64":"eyJzY2hlbWFfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9jbG91ZC1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy81NjY3MTkwZC02NDBjLTM2YWYtYTlmMS1mNGVkMjU4N2U3NjYvc2NoZW1hIiwiY3JlZF9kZWZfaWQiOiJodHRwOi8vMTkyLjE2OC42OC4xMTM6ODAwMC9jbG91ZC1hZ2VudC9jcmVkZW50aWFsLWRlZmluaXRpb24tcmVnaXN0cnkvZGVmaW5pdGlvbnMvM2RhNmExZDgtMmIyMy0zMTM4LWIwMmEtYWIwYmI0OGY4MGY5L2RlZmluaXRpb24iLCJrZXlfY29ycmVjdG5lc3NfcHJvb2YiOnsiYyI6IjM0MjE0MzM4OTA5MDk4MTU5ODIyNTA3MjQ4Njk3NjUyNTIyMzc1NzM2ODM0OTM1MDg0NjM5MTYzNTUyNjgwMjc3MjQyOTcxODI5ODUwIiwieHpfY2FwIjoiODAwNzA3NDcxNzUyOTQ3MTI1NDIxNTU2ODI1ODYyNDc5NzE1OTE0OTE3ODE4NTY4MDI1NjU5Mzk1NDcyMTAzMzQ0NjAwMDI4NzU4MzczMTA5Mjg0NzEzNDg5MDg1OTk3NzE5NDcwOTc5MDQ1NzA3NTY1ODA4MDczNzYxMjI0OTI3MzcyMDk1MTU2MzAxODE3MzgzNDE5NzE4NzM0NDk2MDQwMjc1MzM2MTg0ODIzNzQ4NTg0NzgzNjIxNzE1NTQwOTI3Nzk0ODQyMTA2NDM1NDEwNzc4MDg2ODg1MTc5MzQzMjEzMjU3NTk2ODM2NjU5NzYwMTI4NzI5ODI5MTk2MzI0MjQwMzgyOTc3MzczNjU3MTA0NjQ5NjE4MjU0MDMzMDk4Njc0OTkxMzIwMzc2NTEyMTUyOTk3Mzg0Mzk5MzY0OTc3MDM4NzU2ODcwOTU3NTcyMDM0NTM0NTY1MDM5OTY0MTYzNDgzNDEyMTEzNzc0NzA5MTU2NTcxMjcxNTI3ODY2NzQwNzU1Nzc2MzIzOTgxNTE5NzEzNjQ1MjQ4Njc5NzgyOTM3NTcyOTI4NDI1NzQ0NjE3MjMwNzk2MzYwNzE5MDA3Mzc0ODgwNTI2ODA4ODIzODg2NTMwMzE2MzgyOTI1MDcxOTYzMjUwMzgyNzU4MDA5Mjk1MzI1NDgzODIyNTg0OTIyMDkwMTcwMTAxOTY5OTk5MTg1MzAxMDA1NDM2MjYyMDI4NDIxOTA0NjU5MzY4OTU3OTQyNTAwNDkxNzI5MjY0MTMzMzE5Nzg0NDIwNjQ0NDUxMTUyNjA2MDI2MDg1MDkwNDYzNzAzMTQzNDczMTcxMjg3OTUxMTM0NTM1MDY0NDc0NjQ5NDE5MzUzMDQ4MzQwOTk4MTY2NjYwNDkyMDE5ODQyNDU2NDM5MTcxNjM2IiwieHJfY2FwIjpbWyJhZ2UiLCI3MjA2NjI4MDAzNzAwMjM5MzM5NjAzNjUxNDQ1MTY2NDE0MjcwNTA0ODM4ODAwMzYxMjE0NjQ3MjA1ODYwOTczMTg4MTQ0OTE2OTkwOTE3NTIyNjU4Mzg3Njg1MTcwMzkyMDcyMjk0MTUxMDUwODk1Njc0MTU0OTYwMDMyMDM0Mzg2MzEzMzY0MTMwMDQwNzI5ODQwMjcxMzcyNjc4NTI0NjE0NjUxNDcxNDU0NTg4MzgwMzI2OTMwODQ0Njc4Nzg3MzA3NzY3ODk2NjY5NTE2MDY1Njc3MDA3MjQyMDEwMjQyMDAwNTg4NjgxMTczNDUxNjg4NTU5MDEwMjQyNTgyMTg4MTY5MTUyNjUwNzY4NzgxNjMyMjgwNTgyODI1NjM3MjY0NzUwMTA3NTU2NDQzNTgyNzMwNDIxMjE3NTI4OTgyNTE5MzA3NzQ0ODAxNTYyNTYyMzQzNTcyNzU4NDEzNjc1NzY0ODQwOTY5MTY3NTE3ODcyNjk2MDY1MDM2MDU1MzgwMDg2NjcyNjUzMDEyMTIxMDk2MTA5OTQyMTg1NjM2ODk3MDE3Mjc5NDg3NjEyNDczNzc4NDUxMjkxMjE3NDg3ODQxOTc1NjI2MDczMjI0ODQ1MjI4NDM1OTk0MjI2MTg1MDc1NDI4MjA3OTg5MzAwMjExMzI5OTM4NjQxMzEwMTk5MjcwNTE0NjA2ODU5NDEzNDY1NzE1MjQyNjk0ODc0ODkwNDAzNDk5MzUxOTIxMDY4OTMwODE1ODY5ODM5NDYyMTE0MDI2MjM3MzY5OTAwMzE2MTA0NzYwMDAwNzk2NjcxOTUzNTAxMjcxMTI3MjM4NzM5NDI0Mjc2ODQyODkwNjQwNDY3NjYxNDEzODQ5Mzc5NzEwNzcxNjg0NzU4NTY1NzY2MDY4NzgwNjY0NjI3MjgiXSxbIm1hc3Rlcl9zZWNyZXQiLCI3ODg0ODQyMDE4MzA5NzY5NTg2MDY2Mjc5NDAwNjAzNjIzNjE4NzcxNTc3MjQ1NDk5NzQ5MzE0MzgyNTUyMzMzMjE4MzA3ODk1NzU4MDk2OTc3NjUzNjQ3MDcwNjk5MDE0OTY4OTUwMDg0OTk2MjMwNDAwODA4OTM0MzQ1MzQwMDcyMzY2NDg5NDYxNDg4MDk0MDgyOTk1OTU3MjUwMTg5NTkxMjg2NDQyMDg4MTMwNDA5MDA4Mjc5MzgxMjUzMDIxOTE0MTc5Nzc5MTAyNTcwNjIxNDQ0MDU0NzcxNjY5Mjk5NjQzNTcwODg5NjY1ODQzOTY2ODA1MjM1ODgxNzQ1OTQ3OTQ4NDQ2ODU1MDY4ODU4ODUzMDg2MTQ5NjMxMjA1ODcwMTIzODc1NDg3MTM0NjAxMDQwODA4Njg2MzQ4NDUwOTA0MTI4MTI4Nzk1MjUzMjczMjU3ODc4NjM4MjAxNTcyOTExMDQxNTQ4NDc0MTMzMDMyMTIyMTMyODExNjQ0NjAzNjg0MDU5MDk2ODM2NjU1NjQzMTI2NDU0NTAwNDM2MTgxNjQxMjkyNjQ4MTQ3MjYxODUyNzY5NjIyMzE5Mjk2NjI0NDU3OTg2NzI5NzMwNzE3NDEyNTE2MzEyNjQwNTM0OTE3NzEwNzE4Njc2MTMwODExMTI2NjQwMTkyODg4NjI2ODI2NTcwNzA1OTUxNTUyODI5NDY2NzY5NjUxNTcxNTI2OTMzNDUyNjY0ODk5NTExNzM0ODk0Njc3OTY5NjI0OTgzODI3MTgzMDg2NjA0NTE0NDE3MDE2MDgxNDE2Nzk0NDgwMDIwNDU2ODMxNzUyNjM2NTk1NzcwNjgwODQ0MDE0MjIyOTc2MjE5NzIzODg1MjAxNTg1ODk0MDQwMDA3MTQ5MjkwMDAxNTc4MjMxMDQiXSxbIm5hbWUiLCI4NjIxNzg3ODk1MzA5MzExOTQ3Njc0OTU5NDA4MjIzMTg5ODgwNDEzNTQ2NzIyOTYyODg5NjI0NzgwMjE1MDc4NDc3OTMxNTk5MDk5MzIwODkyNzY2NjM4NjExNDYyOTMzNTg3NjgwMTU0ODQ4ODgxMTY5MzY5NTc3OTk1NTI1ODQ2NDA1NjcyNDUyMzIyMTcyNjQ4MTc4OTEwMTg2ODkwNzYwNzM2MjMwNzA0MDA3NzU1OTA0OTIyNTUwODQ0MjkxNzgwOTk1NDAyNzUyNTU0NTAwNjg1NTY5NzYzMDc4ODY5NDU0NDI3NzY5NzU0Mzc5ODg5NzAzODQzNDM4ODcyMjMyOTc0MjIzODc5MzY5MTYzOTI1NjY3NjY5MzQyOTUwOTk5MjMwOTY1NDQ1MTkwOTM5Mzk5NzM1NjE1MTk2OTY2MzUyMTMwMzQ5MTE0NDE5OTIwMTk3NDIyMjA0ODQ2MTc2OTI3NTMwMDQ3NDkxNjI1NzAwODQ5NDc1MzQzNzk3MjU0MDYwNjc3MTA4MzkxOTU3MzU0MDAzOTAyNzMzMzEzMDI1ODE5Njk3MTIzMTc2NTg3MTU5NzQ5ODkxODg5MzU3Mjk0OTUyNDMwMDY2MTE5MjgzNzA1NTAwMTcxNTc3ODMzMzk3OTE1Mzc5OTA2NTA1MDExMjczODM2NTM3OTA2NjkyMjg2MTk5NDgzMjA3NDc1MzM2MjE1Njc4MTA1NDY5MDc3MTMyNDAwNDM4NTgyOTAyMjMwMzI5MDc4NjA4NTI4NDgzNDEwODI1NDkzNDcwMjI5MjA3MzA1NTk0ODUwMTg4ODUwMjEyNTQ0NzI0NDgyOTExOTQ3NTQ4NzIzMzMxMzYyNTI3MjA0Mzg3MzE1Mjk1MDU4Njk5MDk3NDc3MjQzMTczOTE0Njk5MDAzOTkzNTQiXV19LCJub25jZSI6IjM2MDY2NjE3NzQwNTA3NzY5NTI5NTMxNyJ9"},"format":"anoncreds/credential-offer@v1.0"}],"thid":"3a1c143b-7ab7-470d-99cf-bc5f31771388","ack":[]}"""
        )
        val offerCredential = OfferCredential.fromMessage(message)
        val subjectDid =
            DID("did:prism:6f23ddace519b68dfc0fa06e992db40f2f3c584af382ce446fa2fd0e042e5dea:CoUBCoIBEjsKB21hc3RlcjAQAUouCglzZWNwMjU2azESIQMcKwEitGbQKtGa-jFXi3m1u7OP2JMukYXQnZk3fQIXvxJDCg9hdXRoZW50aWNhdGlvbjAQBEouCglzZWNwMjU2azESIQMcKwEitGbQKtGa-jFXi3m1u7OP2JMukYXQnZk3fQIXvw")

        val requestCredential =
            agent.prepareRequestCredentialWithIssuer(did = subjectDid, offer = offerCredential)

        assertEquals(offerCredential.from, requestCredential.to)
        assertEquals(offerCredential.to, requestCredential.from)
        assertTrue(requestCredential.attachments.size == 1)
        assertEquals(requestCredential.attachments[0].format, CredentialType.ANONCREDS_REQUEST.type)
        assertEquals(offerCredential.thid, requestCredential.thid)
    }

    @Test
    fun testPrepareRequestCredentialWithIssuer_whenJwtOfferCredential_thenProcessed() = runTest {
        val seed =
            Seed("Rb8j6NVmA120auCQT6tP35rZ6-hgHvhcZCYmKmU1Avc4b5Tc7XoPeDdSWZYjLXuHn4w0f--Ulm1WkU1tLzwUEA".base64UrlDecodedBytes)
        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManagerOld,
            seed = seed,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = PrismLoggerMock(),
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
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManagerOld,
            seed = seed,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = PrismLoggerMock(),
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
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManagerOld,
            seed = seed,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = PrismLoggerMock(),
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
        val pollux = PolluxImpl(apolloMock, castorMockOld, apiMock)
        plutoMockOld.getLinkSecretReturn = flow { emit(LinkSecret().getValue()) }
        val meta = CredentialRequestMeta(
            linkSecretName = "1",
            json = "{\"link_secret_blinding_data\":{\"v_prime\":\"1234\",\"vr_prime\":\"1234\"},\"nonce\":\"411729288962137159046778\",\"link_secret_name\":\"link:secret:id\"}"
        )
        plutoMockOld.getCredentialMetadataReturn = flow { emit(meta) }

        val agent = EdgeAgent(
            apollo = apolloMock,
            castor = castorMockOld,
            pluto = plutoMockOld,
            mercury = mercuryMock,
            pollux = pollux,
            connectionManager = connectionManagerOld,
            seed = null,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = PrismLoggerMock(),
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
    fun testInitiatePresentationRequest_whenAllCorrect_thenMessageSentCorrectly() = runTest {
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
            logger = PrismLoggerMock()
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
            "{\"presentation_definition\":{\"id\":\"32f54163-7166-48f1-93d8-ff217bdb0653\",\"input_descriptors\":[{\"id\":\"wa_driver_license\",\"name\":\"Washington State Business License\",\"purpose\":\"We can only allow licensed Washington State business representatives into the WA Business Conference\",\"constraints\":{\"fields\":[{\"path\":[\"$.credentialSubject.dateOfBirth\",\"$.credentialSubject.dob\",\"$.vc.credentialSubject.dateOfBirth\",\"$.vc.credentialSubject.dob\"]}]}}],\"format\":{\"jwt\":{\"alg\":[\"ES256K\"]}}},\"options\":{\"domain\":\"domain\",\"challenge\":\"challenge\"}}"
        val presentationDefinitionRequest: PresentationDefinitionRequest =
            Json.decodeFromString(definitionJson)
        // Mock createPresentationDefinitionRequest
        `when`(polluxMock.createPresentationDefinitionRequest(any(), any(), any())).thenReturn(
            presentationDefinitionRequest
        )

        val toDid = "did:peer:fdsafdsa"
        val credentialType = CredentialType.JWT
        agent.initiatePresentationRequest(
            type = credentialType,
            toDID = DID(toDid),
            presentationClaims = PresentationClaims(
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
                logger = PrismLoggerMock()
            )
            val msg = Json.decodeFromString<Message>(
                "{\"id\":\"00000000-685c-4004-0000-000036ac64ee\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"to\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-08T19:27:38.196506Z\",\"expiresTimePlus\":\"2024-03-09T19:27:38.196559Z\",\"attachments\":[{\"id\":\"00000000-9c2e-4249-0000-0000c1176949\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64\",\"base64\":\"eyJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjMyZjU0MTYzLTcxNjYtNDhmMS05M2Q4LWZmMjE3YmRiMDY1MyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IndhX2RyaXZlcl9saWNlbnNlIiwibmFtZSI6Ildhc2hpbmd0b24gU3RhdGUgQnVzaW5lc3MgTGljZW5zZSIsInB1cnBvc2UiOiJXZSBjYW4gb25seSBhbGxvdyBsaWNlbnNlZCBXYXNoaW5ndG9uIFN0YXRlIGJ1c2luZXNzIHJlcHJlc2VudGF0aXZlcyBpbnRvIHRoZSBXQSBCdXNpbmVzcyBDb25mZXJlbmNlIiwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiIsIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiJdfV19fV0sImZvcm1hdCI6eyJqd3QiOnsiYWxnIjpbIkVTMjU2SyJdfX19LCAib3B0aW9ucyI6IHsiZG9tYWluIjogImRvbWFpbiIsICJjaGFsbGVuZ2UiOiAiY2hhbGxlbmdlIn19\"},\"format\":\"dif/presentation-exchange/definitions@v1.0\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
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
    fun testHandlePresentationDefinitionRequest_whenWrongAttachmentType_thenThrowPresentationRequestAttachmentNotSupported() =
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
                logger = PrismLoggerMock()
            )
            val msg = Json.decodeFromString<Message>(
                "{\"id\":\"00000000-685c-4004-0000-000036ac64ee\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"to\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-08T19:27:38.196506Z\",\"expiresTimePlus\":\"2024-03-09T19:27:38.196559Z\",\"attachments\":[{\"id\":\"00000000-9c2e-4249-0000-0000c1176949\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentJsonData\",\"data\":\"eyJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjMyZjU0MTYzLTcxNjYtNDhmMS05M2Q4LWZmMjE3YmRiMDY1MyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IndhX2RyaXZlcl9saWNlbnNlIiwibmFtZSI6Ildhc2hpbmd0b24gU3RhdGUgQnVzaW5lc3MgTGljZW5zZSIsInB1cnBvc2UiOiJXZSBjYW4gb25seSBhbGxvdyBsaWNlbnNlZCBXYXNoaW5ndG9uIFN0YXRlIGJ1c2luZXNzIHJlcHJlc2VudGF0aXZlcyBpbnRvIHRoZSBXQSBCdXNpbmVzcyBDb25mZXJlbmNlIiwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiIsIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiJdfV19fV0sImZvcm1hdCI6eyJqd3QiOnsiYWxnIjpbIkVTMjU2SyJdfX19LCAib3B0aW9ucyI6IHsiZG9tYWluIjogImRvbWFpbiIsICJjaGFsbGVuZ2UiOiAiY2hhbGxlbmdlIn19\"},\"format\":\"dif/presentation-exchange/definitions@v1.0\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
            )
            val credential = JWTCredential.fromJwtString(
                "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng"
            )
            assertFailsWith(EdgeAgentError.AttachmentTypeNotSupported::class) {
                agent.preparePresentationForRequestProof(
                    RequestPresentation.fromMessage(msg),
                    credential
                )
            }
        }

    @Test
    fun testHandlePresentationDefinitionRequest_whenAllCorrect_thenSendPresentationSubmissionCorrectly() =
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

            val privateKey = Secp256k1KeyPair.generateKeyPair(
                seed = Seed(MnemonicHelper.createRandomSeed()),
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
            `when`(apolloMock.restorePrivateKey(storablePrivateKeys.first())).thenReturn(privateKey)

            val presentationSubmissionString =
                "{\"presentation_submission\":{\"id\":\"00000000-c224-45d7-0000-0000732f4932\",\"definition_id\":\"32f54163-7166-48f1-93d8-ff217bdb0653\",\"descriptor_map\":[{\"id\":\"wa_driver_license\",\"format\":\"jwt\",\"path\":\"$.verifiablePresentation[0]\"}]},\"verifiablePresentation\":[\"eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng\"]}"
            val presentationSubmission = Json.decodeFromString<PresentationSubmission>(
                presentationSubmissionString
            )
            // Mock createPresentationSubmission response
            `when`(polluxMock.createPresentationSubmission(any(), any(), any())).thenReturn(
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
                logger = PrismLoggerMock()
            )
            val msg = Json.decodeFromString<Message>(
                "{\"id\":\"00000000-685c-4004-0000-000036ac64ee\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"to\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-08T19:27:38.196506Z\",\"expiresTimePlus\":\"2024-03-09T19:27:38.196559Z\",\"attachments\":[{\"id\":\"00000000-9c2e-4249-0000-0000c1176949\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64\",\"base64\":\"eyJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjMyZjU0MTYzLTcxNjYtNDhmMS05M2Q4LWZmMjE3YmRiMDY1MyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IndhX2RyaXZlcl9saWNlbnNlIiwibmFtZSI6Ildhc2hpbmd0b24gU3RhdGUgQnVzaW5lc3MgTGljZW5zZSIsInB1cnBvc2UiOiJXZSBjYW4gb25seSBhbGxvdyBsaWNlbnNlZCBXYXNoaW5ndG9uIFN0YXRlIGJ1c2luZXNzIHJlcHJlc2VudGF0aXZlcyBpbnRvIHRoZSBXQSBCdXNpbmVzcyBDb25mZXJlbmNlIiwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiIsIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiJdfV19fV0sImZvcm1hdCI6eyJqd3QiOnsiYWxnIjpbIkVTMjU2SyJdfX19LCAib3B0aW9ucyI6IHsiZG9tYWluIjogImRvbWFpbiIsICJjaGFsbGVuZ2UiOiAiY2hhbGxlbmdlIn19\"},\"format\":\"dif/presentation-exchange/definitions@v1.0\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
            )
            val credential = JWTCredential.fromJwtString(
                "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng"
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
            assertEquals(
                presentationSubmissionString.base64UrlEncoded,
                attachmentData.base64
            )
        }

    @Test
    fun testPreparePresentationForRequestProof_whenJWTProof_thenSendPresentationCorrectly() =
        runTest {
            val apiMock = mock<Api>()
            `when`(apiMock.request(any(), any(), any(), any(), any()))
                .thenReturn(HttpResponse(200, "Ok"))

            val apolloMock = mock<Apollo>()
            val castorMock = mock<Castor>()
            val plutoMock = mock<Pluto>()
            val mercuryMock = mock<Mercury>()
            val polluxMock = mock<Pollux>()
            val provableCredentialMock = mock<ProvableCredential>()
            val connectionManagerMock = mock<ConnectionManager>()
            val seed = Seed(MnemonicHelper.createRandomSeed())

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
                mercury = mercuryMock,
                pollux = polluxMock,
                connectionManager = connectionManagerMock,
                seed = seed,
                api = apiMock,
                logger = PrismLoggerMock()
            )

            val credential = JWTCredential.fromJwtString(
                "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MzM3ZThmZTE0NGFhY2VkM2NhM2RkNTk0NjI0MDRmNDU5OTZlM2IyMjFhYmM0MTBhNzI1ZWE2NjUzNDg5NzJiYjpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF2eWcxYTN1cHVmbFBLczhKR1hKU3NxV1pjVG9GQXk3RjNSTFBjQlk0V25zRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFPYVBUbzM5Tnh2UmhXUW5iVWhoTXM5bTFIeEJtcV9hZWNHM0tTTGZiNWgzUkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVEZ3poOElDY1ZhNVlNZjYzRFFaM191dTNOMzNsSXVGSGJoX09KUlVIbWd2YyIsInN1YiI6ImRpZDpwcmlzbTpiZDgxZmY1NDQzNDJjMTAwNDZkZmE0YmEyOTVkNWIzNmU0Y2ZlNWE3ZWIxMjBlMTBlZTVjMjQ4NzAwNjUxMDA5OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdBIiwibmJmIjoxNzE4MjI1MDUyLCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJlbWFpbEFkZHJlc3MiOiJkZW1vQGVtYWlsLmNvbSIsImRyaXZpbmdDbGFzcyI6IjEiLCJmYW1pbHlOYW1lIjoiZGVtbyIsImRyaXZpbmdMaWNlbnNlSUQiOiJBMTIyMTMzMiIsImlkIjoiZGlkOnByaXNtOmJkODFmZjU0NDM0MmMxMDA0NmRmYTRiYTI5NWQ1YjM2ZTRjZmU1YTdlYjEyMGUxMGVlNWMyNDg3MDA2NTEwMDk6Q29VQkNvSUJFanNLQjIxaGMzUmxjakFRQVVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdCSkRDZzloZFhSb1pXNTBhV05oZEdsdmJqQVFCRW91Q2dselpXTndNalUyYXpFU0lRUHY1UDV5eWdyWndhSmxZemw1OW0ySEFES1RYVUxQc1JlMGtnZUVIdnRMZ0EiLCJkYXRlT2ZJc3N1YW5jZSI6IjAxXC8wMVwvMjAyNCJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6NCwiaWQiOiJodHRwOlwvXC8xOTIuMTY4LjY4LjExMzo4MDAwXC9wcmlzbS1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzM5YjBiNzI2LTBmNmUtNDlmNy05YzUyLTYyYTc4MTcxNzVlOCM0IiwidHlwZSI6IlN0YXR1c0xpc3QyMDIxRW50cnkiLCJzdGF0dXNMaXN0Q3JlZGVudGlhbCI6Imh0dHA6XC9cLzE5Mi4xNjguNjguMTEzOjgwMDBcL3ByaXNtLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvMzliMGI3MjYtMGY2ZS00OWY3LTljNTItNjJhNzgxNzE3NWU4In19fQ.XuMkGkJQCM5214ZjxwdHf81Jox_qyGsh011OmRtda8lTsh6TFPy9jDNey0DVijCg12qDTj-cYcFUAXe6pfvoPQ"
            )

            val pathIndexFlow = flow { emit(1) }
            `when`(plutoMock.getPrismDIDKeyPathIndex(DID(credential.subject!!))).thenReturn(pathIndexFlow)

            val testVerifiablePresentationJWTPayload = "testPayload"
            `when`(provableCredentialMock.presentation(any(), any())).thenReturn(testVerifiablePresentationJWTPayload)

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
                attachmentData.base64.base64UrlDecoded.split(".").count()
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

            val privateKey =
                Secp256k1KeyPair.generateKeyPair(
                    seed = Seed(MnemonicHelper.createRandomSeed()),
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
            `when`(apolloMock.restorePrivateKey(storablePrivateKeys.first())).thenReturn(privateKey)

            val presentationSubmission = Json.decodeFromString<PresentationSubmission>(
                "{\"presentation_submission\":{\"id\":\"00000000-c224-45d7-0000-0000732f4932\",\"definition_id\":\"32f54163-7166-48f1-93d8-ff217bdb0653\",\"descriptor_map\":[{\"id\":\"wa_driver_license\",\"format\":\"jwt\",\"path\":\"$.verifiablePresentation[0]\"}]},\"verifiablePresentation\":[\"eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng\"]}"
            )
            // Mock createPresentationSubmission response
            `when`(polluxMock.createPresentationSubmission(any(), any(), any())).thenReturn(
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
                logger = PrismLoggerMock()
            )

            val msgString =
                "{\"id\":\"00000000-621a-4ae9-0000-00002ffb05bf\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"to\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-18T17:11:58.053680Z\",\"expiresTimePlus\":\"2024-03-19T17:11:58.058523Z\",\"attachments\":[{\"id\":\"00000000-ef5f-40c0-0000-0000d2674b80\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentJsonData\",\"data\":\"eyJwcmVzZW50YXRpb25fc3VibWlzc2lvbiI6eyJpZCI6IjAwMDAwMDAwLWMyMjQtNDVkNy0wMDAwLTAwMDA3MzJmNDkzMiIsImRlZmluaXRpb25faWQiOiIzMmY1NDE2My03MTY2LTQ4ZjEtOTNkOC1mZjIxN2JkYjA2NTMiLCJkZXNjcmlwdG9yX21hcCI6W3siaWQiOiJ3YV9kcml2ZXJfbGljZW5zZSIsImZvcm1hdCI6Imp3dF92cCIsInBhdGgiOiIkLnZlcmlmaWFibGVDcmVkZW50aWFsWzBdIn1dfSwidmVyaWZpYWJsZUNyZWRlbnRpYWwiOlt7InZjIjp7ImNvbnRleHQiOltdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImFkZGl0aW9uYWxQcm9wMiI6IlRlc3QzIiwiaWQiOiJkaWQ6cHJpc206YmVlYTUyMzRhZjQ2ODA0NzE0ZDhlYThlYzc3YjY2Y2M3ZjNlODE1YzY4YWJiNDc1ZjI1NGNmOWMzMDYyNjc2MzpDc2NCQ3NRQkVtUUtEMkYxZEdobGJuUnBZMkYwYVc5dU1CQUVRazhLQ1hObFkzQXlOVFpyTVJJZ2VTZy0yT08xSmRucHpVT0JpdHpJaWNYZGZ6ZUFjVGZXQU4tWUNldUNieUlhSUpRNEdUSTMwdGFWaXdjaFQzZTBuTFhCUzQzQjRqOWpsc2xLbzJabGRYempFbHdLQjIxaGMzUmxjakFRQVVKUENnbHpaV053TWpVMmF6RVNJSGtvUHRqanRTWFo2YzFEZ1lyY3lJbkYzWDgzZ0hFMzFnRGZtQW5yZ204aUdpQ1VPQmt5TjlMV2xZc0hJVTkzdEp5MXdVdU53ZUlfWTViSlNxTm1aWFY4NHcifX19XSwicHJvb2YiOnsidHlwZSI6IkVjZHNhU2VjcDI1NmsxU2lnbmF0dXJlMjAxOSIsImNyZWF0ZWQiOiIyOCBKdW5lIDU2MTU1LCAwNzozMToxMCIsInByb29mUHVycG9zZSI6ImF1dGhlbnRpY2F0aW9uIiwidmVyaWZpY2F0aW9uTWV0aG9kIjoiZGlkOnByaXNtOmFzZGZhc2RmYXNkZmFzZGYja2V5cy0xIiwiandzIjoiZXlKaGJHY2lPaUpGVXpJMU5rc2lmUS5leUpwYzNNaU9pSmthV1E2Y0hKcGMyMDZNalUzTVRsaE9UWmlNVFV4TWpBM01UWTVPREZoT0RRek1HRmtNR05pT1RZNFpHUTFNelF3TnpNMU9UTmpPR05rTTJZeFpESTNZVFk0TURSbFl6VXdaVHBEY0c5RFEzQmpRMFZzYjB0Q1YzUnNaVk13ZUVWQlNrTlVkMjlLWXpKV2FtTkVTVEZPYlhONFJXbEJSVzlUUTI0MWRIbEVZVFpaTm5JdFNXMVRjWEJLT0ZreGJXbzNTa016WDI5VmVrVXdUbmw1UldsRFFtOW5jMmRPWVdWU1pHTkRVa2RRYkdVNE1sWjJPWFJLWms1M2JEWnlaelpXWTJoU00wOXhhR2xXWWxSaE9GTlhkMjlIV1ZoV01HRkRNSGhGUVZKRFZIZHZTbU15Vm1walJFa3hUbTF6ZUVWcFJFMXJRbVEyUm5ScGIwcHJNMWhQUm5VdFgyTjVOVmh0VWkwMGRGVlJNazVNUjJsWE9HRkpVMjl0YTFKdlp6WlRaR1U1VUhkdVJ6QlJNRk5DVkcxR1UxUkVZbE5MUW5aSlZqWkRWRXhZY21wSlNuUjBaVWRKYlVGVFdFRnZTR0pYUm5wa1IxWjVUVUpCUWxGck9FdERXRTVzV1ROQmVVNVVXbkpOVWtsblR6Y3hNRzEwTVZkZmFYaEVlVkZOTTNoSmN6ZFVjR3BNUTA1UFJGRjRaMVpvZURWemFHWkxUbGd4YjJGSlNGZFFjbmMzU1ZWTGJHWnBZbEYwZURaS2F6UlVVMnBuWTFkT1QyWmpUM1JWT1VRNVVIVmFOMVE1ZENJc0luTjFZaUk2SW1ScFpEcHdjbWx6YlRwaVpXVmhOVEl6TkdGbU5EWTRNRFEzTVRSa09HVmhPR1ZqTnpkaU5qWmpZemRtTTJVNE1UVmpOamhoWW1JME56Vm1NalUwWTJZNVl6TXdOakkyTnpZek9rTnpZMEpEYzFGQ1JXMVJTMFF5UmpGa1IyaHNZbTVTY0ZreVJqQmhWemwxVFVKQlJWRnJPRXREV0U1c1dUTkJlVTVVV25KTlVrbG5aVk5uTFRKUFR6RktaRzV3ZWxWUFFtbDBla2xwWTFoa1pucGxRV05VWmxkQlRpMVpRMlYxUTJKNVNXRkpTbEUwUjFSSk16QjBZVlpwZDJOb1ZETmxNRzVNV0VKVE5ETkNOR281YW14emJFdHZNbHBzWkZoNmFrVnNkMHRDTWpGb1l6TlNiR05xUVZGQlZVcFFRMmRzZWxwWFRuZE5hbFV5WVhwRlUwbElhMjlRZEdwcWRGTllXalpqTVVSbldYSmplVWx1UmpOWU9ETm5TRVV6TVdkRVptMUJibkpuYlRocFIybERWVTlDYTNsT09VeFhiRmx6U0VsVk9UTjBTbmt4ZDFWMVRuZGxTVjlaTldKS1UzRk9iVnBZVmpnMGR5SXNJbTVpWmlJNk1UWTROVFl6TVRrNU5Td2laWGh3SWpveE5qZzFOak0xTlRrMUxDSjJZeUk2ZXlKamNtVmtaVzUwYVdGc1UzVmlhbVZqZENJNmV5SmhaR1JwZEdsdmJtRnNVSEp2Y0RJaU9pSlVaWE4wTXlJc0ltbGtJam9pWkdsa09uQnlhWE50T21KbFpXRTFNak0wWVdZME5qZ3dORGN4TkdRNFpXRTRaV00zTjJJMk5tTmpOMll6WlRneE5XTTJPR0ZpWWpRM05XWXlOVFJqWmpsak16QTJNalkzTmpNNlEzTmpRa056VVVKRmJWRkxSREpHTVdSSGFHeGlibEp3V1RKR01HRlhPWFZOUWtGRlVXczRTME5ZVG14Wk0wRjVUbFJhY2sxU1NXZGxVMmN0TWs5UE1VcGtibkI2VlU5Q2FYUjZTV2xqV0dSbWVtVkJZMVJtVjBGT0xWbERaWFZEWW5sSllVbEtVVFJIVkVrek1IUmhWbWwzWTJoVU0yVXdia3hZUWxNME0wSTBhamxxYkhOc1MyOHlXbXhrV0hwcVJXeDNTMEl5TVdoak0xSnNZMnBCVVVGVlNsQkRaMng2V2xkT2QwMXFWVEpoZWtWVFNVaHJiMUIwYW1wMFUxaGFObU14UkdkWmNtTjVTVzVHTTFnNE0yZElSVE14WjBSbWJVRnVjbWR0T0dsSGFVTlZUMEpyZVU0NVRGZHNXWE5JU1ZVNU0zUktlVEYzVlhWT2QyVkpYMWsxWWtwVGNVNXRXbGhXT0RSM0luMHNJblI1Y0dVaU9sc2lWbVZ5YVdacFlXSnNaVU55WldSbGJuUnBZV3dpWFN3aVFHTnZiblJsZUhRaU9sc2lhSFIwY0hNNlhDOWNMM2QzZHk1M015NXZjbWRjTHpJd01UaGNMMk55WldSbGJuUnBZV3h6WEM5Mk1TSmRmWDAueDBTRjE3WTBWQ0RtdDdIY2VPZFR4Zkhsb2ZzWm1ZMThSbjZWUWIwLXIta19CbTNoVGkxLWsydmtkakIyNWhkeHlUQ3Z4YW0tQWtBUC1BZzNBaG41TmciLCJjaGFsbGVuZ2UiOiIzMDQ1MDIyMTAwYjE0MTJjMGYzZmJiYzVjODc2ZGRlNjExNDFmYTY4N2Y3ZjJmYWJhODM0YWJjZTA5Yzg2YzcwNWEwYjkwMjAwNTAyMjA2YjY3MjUzZmE1ZjgwMzQ0YzQyZGQ4NGQyMzZiYmJiMTVkNTBhODliODE2ZmE1NWQ1YTZhNzQyY2NjODYwZTIzIn19\"},\"format\":\"prism/jwt\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
            val msg = Json.decodeFromString<Message>(msgString)

            assertFailsWith<EdgeAgentError.AttachmentTypeNotSupported> {
                agent.handlePresentation(msg)
            }
        }

    @Test
    fun testHandlePresentationSubmission_whenAllCorrect_thenReturnTrue() = runTest {
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

        val privateKey = Secp256k1KeyPair.generateKeyPair(
            seed = Seed(MnemonicHelper.createRandomSeed()),
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
        `when`(apolloMock.restorePrivateKey(storablePrivateKeys.first())).thenReturn(privateKey)

        val requestMsg = Json.decodeFromString<Message>(
            "{\"id\":\"00000000-685c-4004-0000-000036ac64ee\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"to\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-08T19:27:38.196506Z\",\"expiresTimePlus\":\"2024-03-09T19:27:38.196559Z\",\"attachments\":[{\"id\":\"00000000-9c2e-4249-0000-0000c1176949\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64\",\"base64\":\"eyJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjMyZjU0MTYzLTcxNjYtNDhmMS05M2Q4LWZmMjE3YmRiMDY1MyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IndhX2RyaXZlcl9saWNlbnNlIiwibmFtZSI6Ildhc2hpbmd0b24gU3RhdGUgQnVzaW5lc3MgTGljZW5zZSIsInB1cnBvc2UiOiJXZSBjYW4gb25seSBhbGxvdyBsaWNlbnNlZCBXYXNoaW5ndG9uIFN0YXRlIGJ1c2luZXNzIHJlcHJlc2VudGF0aXZlcyBpbnRvIHRoZSBXQSBCdXNpbmVzcyBDb25mZXJlbmNlIiwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiIsIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZGF0ZU9mQmlydGgiLCIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0LmRvYiJdfV19fV0sImZvcm1hdCI6eyJqd3QiOnsiYWxnIjpbIkVTMjU2SyJdfX19LCAib3B0aW9ucyI6IHsiZG9tYWluIjogImRvbWFpbiIsICJjaGFsbGVuZ2UiOiAiY2hhbGxlbmdlIn19\"},\"format\":\"dif/presentation-exchange/definitions@v1.0\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
        )
        `when`(
            plutoMock.getMessageByThidAndPiuri(
                any(),
                any()
            )
        ).thenReturn(flow { emit(requestMsg) })

        val presentationSubmission = Json.decodeFromString<PresentationSubmission>(
            "{\"presentation_submission\":{\"id\":\"015a6303-3a16-4813-a657-54a12ff5dab4\",\"definition_id\":\"32f54163-7166-48f1-93d8-ff217bdb0653\",\"descriptor_map\":[{\"id\":\"wa_driver_license\",\"format\":\"jwt\",\"path\":\"$.verifiablePresentation[0]\"}]},\"verifiablePresentation\":[\"eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng\"]}"
        )
        // Mock createPresentationSubmission response
        `when`(polluxMock.createPresentationSubmission(any(), any(), any())).thenReturn(
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
            logger = PrismLoggerMock()
        )

        val msgString =
            "{\"id\":\"00000000-621a-4ae9-0000-00002ffb05bf\",\"piuri\":\"https://didcomm.atalaprism.io/present-proof/3.0/presentation\",\"from\":{\"method\":\"peer\",\"methodId\":\"fdsafdsa\"},\"to\":{\"method\":\"peer\",\"methodId\":\"asdfasdf\"},\"fromPrior\":null,\"body\":\"{}\",\"createdTime\":\"2024-03-18T17:11:58.053680Z\",\"expiresTimePlus\":\"2024-03-19T17:11:58.058523Z\",\"attachments\":[{\"id\":\"00000000-ef5f-40c0-0000-0000d2674b80\",\"mediaType\":\"application/json\",\"data\":{\"type\":\"org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64\",\"base64\":\"eyJwcmVzZW50YXRpb25fc3VibWlzc2lvbiI6eyJpZCI6IjAwMDAwMDAwLWMyMjQtNDVkNy0wMDAwLTAwMDA3MzJmNDkzMiIsImRlZmluaXRpb25faWQiOiIzMmY1NDE2My03MTY2LTQ4ZjEtOTNkOC1mZjIxN2JkYjA2NTMiLCJkZXNjcmlwdG9yX21hcCI6W3siaWQiOiJ3YV9kcml2ZXJfbGljZW5zZSIsImZvcm1hdCI6Imp3dCIsInBhdGgiOiIkLnZlcmlmaWFibGVQcmVzZW50YXRpb25bMF0ifV19LCJ2ZXJpZmlhYmxlUHJlc2VudGF0aW9uIjpbImV5SmhiR2NpT2lKRlV6STFOa3NpZlEuZXlKcGMzTWlPaUprYVdRNmNISnBjMjA2TWpVM01UbGhPVFppTVRVeE1qQTNNVFk1T0RGaE9EUXpNR0ZrTUdOaU9UWTRaR1ExTXpRd056TTFPVE5qT0dOa00yWXhaREkzWVRZNE1EUmxZelV3WlRwRGNHOURRM0JqUTBWc2IwdENWM1JzWlZNd2VFVkJTa05VZDI5S1l6SldhbU5FU1RGT2JYTjRSV2xCUlc5VFEyNDFkSGxFWVRaWk5uSXRTVzFUY1hCS09Ga3hiV28zU2tNelgyOVZla1V3VG5sNVJXbERRbTluYzJkT1lXVlNaR05EVWtkUWJHVTRNbFoyT1hSS1prNTNiRFp5WnpaV1kyaFNNMDl4YUdsV1lsUmhPRk5YZDI5SFdWaFdNR0ZETUhoRlFWSkRWSGR2U21NeVZtcGpSRWt4VG0xemVFVnBSRTFyUW1RMlJuUnBiMHByTTFoUFJuVXRYMk41TlZodFVpMDBkRlZSTWs1TVIybFhPR0ZKVTI5dGExSnZaelpUWkdVNVVIZHVSekJSTUZOQ1ZHMUdVMVJFWWxOTFFuWkpWalpEVkV4WWNtcEpTblIwWlVkSmJVRlRXRUZ2U0dKWFJucGtSMVo1VFVKQlFsRnJPRXREV0U1c1dUTkJlVTVVV25KTlVrbG5UemN4TUcxME1WZGZhWGhFZVZGTk0zaEpjemRVY0dwTVEwNVBSRkY0WjFab2VEVnphR1pMVGxneGIyRkpTRmRRY25jM1NWVkxiR1pwWWxGMGVEWkthelJVVTJwblkxZE9UMlpqVDNSVk9VUTVVSFZhTjFRNWRDSXNJbk4xWWlJNkltUnBaRHB3Y21semJUcGlaV1ZoTlRJek5HRm1ORFk0TURRM01UUmtPR1ZoT0dWak56ZGlOalpqWXpkbU0yVTRNVFZqTmpoaFltSTBOelZtTWpVMFkyWTVZek13TmpJMk56WXpPa056WTBKRGMxRkNSVzFSUzBReVJqRmtSMmhzWW01U2NGa3lSakJoVnpsMVRVSkJSVkZyT0V0RFdFNXNXVE5CZVU1VVduSk5Va2xuWlZObkxUSlBUekZLWkc1d2VsVlBRbWwwZWtscFkxaGtabnBsUVdOVVpsZEJUaTFaUTJWMVEySjVTV0ZKU2xFMFIxUkpNekIwWVZacGQyTm9WRE5sTUc1TVdFSlRORE5DTkdvNWFteHpiRXR2TWxwc1pGaDZha1ZzZDB0Q01qRm9Zek5TYkdOcVFWRkJWVXBRUTJkc2VscFhUbmROYWxVeVlYcEZVMGxJYTI5UWRHcHFkRk5ZV2paak1VUm5XWEpqZVVsdVJqTllPRE5uU0VVek1XZEVabTFCYm5KbmJUaHBSMmxEVlU5Q2EzbE9PVXhYYkZselNFbFZPVE4wU25reGQxVjFUbmRsU1Y5Wk5XSktVM0ZPYlZwWVZqZzBkeUlzSW01aVppSTZNVFk0TlRZek1UazVOU3dpWlhod0lqb3hOamcxTmpNMU5UazFMQ0oyWXlJNmV5SmpjbVZrWlc1MGFXRnNVM1ZpYW1WamRDSTZleUpoWkdScGRHbHZibUZzVUhKdmNESWlPaUpVWlhOME15SXNJbWxrSWpvaVpHbGtPbkJ5YVhOdE9tSmxaV0UxTWpNMFlXWTBOamd3TkRjeE5HUTRaV0U0WldNM04ySTJObU5qTjJZelpUZ3hOV00yT0dGaVlqUTNOV1l5TlRSalpqbGpNekEyTWpZM05qTTZRM05qUWtOelVVSkZiVkZMUkRKR01XUkhhR3hpYmxKd1dUSkdNR0ZYT1hWTlFrRkZVV3M0UzBOWVRteFpNMEY1VGxSYWNrMVNTV2RsVTJjdE1rOVBNVXBrYm5CNlZVOUNhWFI2U1dsaldHUm1lbVZCWTFSbVYwRk9MVmxEWlhWRFlubEpZVWxLVVRSSFZFa3pNSFJoVm1sM1kyaFVNMlV3Ymt4WVFsTTBNMEkwYWpscWJITnNTMjh5V214a1dIcHFSV3gzUzBJeU1XaGpNMUpzWTJwQlVVRlZTbEJEWjJ4NldsZE9kMDFxVlRKaGVrVlRTVWhyYjFCMGFtcDBVMWhhTm1NeFJHZFpjbU41U1c1R00xZzRNMmRJUlRNeFowUm1iVUZ1Y21kdE9HbEhhVU5WVDBKcmVVNDVURmRzV1hOSVNWVTVNM1JLZVRGM1ZYVk9kMlZKWDFrMVlrcFRjVTV0V2xoV09EUjNJbjBzSW5SNWNHVWlPbHNpVm1WeWFXWnBZV0pzWlVOeVpXUmxiblJwWVd3aVhTd2lRR052Ym5SbGVIUWlPbHNpYUhSMGNITTZYQzljTDNkM2R5NTNNeTV2Y21kY0x6SXdNVGhjTDJOeVpXUmxiblJwWVd4elhDOTJNU0pkZlgwLngwU0YxN1kwVkNEbXQ3SGNlT2RUeGZIbG9mc1ptWTE4Um42VlFiMC1yLWtfQm0zaFRpMS1rMnZrZGpCMjVoZHh5VEN2eGFtLUFrQVAtQWczQWhuNU5nIl19\"},\"format\":\"prism/jwt\"}],\"thid\":\"00000000-ef9d-4722-0000-00003b1bc908\",\"ack\":[]}"
        val msg = Json.decodeFromString<Message>(msgString)

        assertTrue(agent.handlePresentation(msg))
    }

    val getCredentialDefinitionResponse =
        "{\"schemaId\":\"http://host.docker.internal:8000/prism-agent/schema-registry/schemas/5e0d5a93-4bfd-3111-a956-5d5bc82f76cc\",\"type\":\"CL\",\"tag\":\"licence\",\"value\":{\"primary\":{\"n\":\"105195159277979097653318357586659371305119697478469834190626350283715795188687389523188659352120689851168860621983864738336838773213022505168653440146374011050277159372491059901432822905781969400722059341786498751125483895348734607382548396665339315322605154516776326303787844694026898270194867398625429469096229269732265502538641116512214652017416624138065704599041020588805936844771273861390913500753293895219370960892829297672575154196820931047049021760519166121287056337193413235473255257349024671869248216238831094979209384406168241010010012567685965827447177652200129684927663161550376084422586141212281146491949\",\"s\":\"85376740935726732134199731472843597191822272986425414914465211197069650618238336366149699822721009443794877925725075553195071288777117865451699414058058985000654277974066307286552934230286237253977472401290858765904161191229985245519871949378628131263513153683765553672655918133136828182050729012388157183851720391379381006921499997765191873729408614024320763554099291141052786589157823043612948619201525441997065264492145372001259366749278235381762443117203343617927241093647322654346302447381494008414208398219626199373278313446814209403507903682881070548386699522575055488393512785511441688197244526708647113340516\",\"r\":{\"dateofissuance\":\"16159515692057558658031632775257139859912833740243870833808276956469677196577164655991169139545328065546186056342530531355718904597216453319851305621683589202769847381737819412615902541110462703838858425423753481085962114120185123089078513531045426316918036549403698066078445947881055316312848598741184161901260446303171175343050250045452903485086185722998336149005743485268486377824763449026501058416292877646187105446333888525480394665310217044483841168928926515929150167890936706159800372381200383816724043496032886366767166850459338411710056171379538841845247931898550165532492578625954615979453881721709564750235\",\"drivingclass\":\"83649701835078373520097916558245060224505938113940626586910000950978790663411517512280043632278010831292224659523658613504637416710001103641231226266903556936380105758523760424939825687213460920436570466066231912959327201876189240504388424799892400351592593406285436824571943165913587899115814843543998396726679289422080229750418336051741708013580146373647528674381958028243228435161765957312248113519708734663989428761879029086059388435772829434952754093999424834120341657211221855300108096057633128467059590470639772605075954658131680801785637700237403873940041665483384938586320674338994185073499523485570537331062\",\"emailaddress\":\"96995643129591814391344614133120459563648002327749700279517548454036811217735867585059116635583558148259032071807493674533230465312311981127622542797279917256478867847832932893748528200469349058284133058865149153179959849308383505167342565738382180666525211256221655129861213392455759272915565057394420728271409215556596974900718332893753172173500744392522771654048192448229319313386967045678744665093451560743782910263014930200762027209565313884859542996067229707388839912195826334964819133016500346618083969320902775088800287566711941842968839787149808739739233388585677095545116231323172342995837636586249573194609\",\"drivinglicenseid\":\"102840929811153624977554462471309185033977661854754815794111114507549576719389525167082631547450413573293352276930065480432301200611396989595571202142654033217842162456070556560693402484110499573693863745648118310258284468114751958738878996458420605301017450868522680454545537837403398645500541915771765220093329728663621098538954397330411649083351383375839056527007892276284168437065687748085384178113959961057476582871100422859953560730152958588610850909069434658487744782540788968302663076149478487413357533660817020800754493642858564081116318655661240523146995256712471572605700346459123074377380656921337264554594\",\"familyname\":\"2428690037146701497427424649573806616639612325136606164619283916796880313617677563507218774958436668407050506838114136163250163675016510113975582318007560622124292458766639319715064358235569650961433812439763343736699708535945693241909905707497180931492818502593885932421170612418693515054756633264933222189766691632082890045477718331705366111669009551578289182848340651375008362238266590844461708981816856194045325523248527964502118319210042254240848590574645476930113881493472578612352948284862674703949781070309344526122291448990325949065193279599181502524961004046979227803224474342778516917124487012958845744311\",\"master_secret\":\"96236339155824229583363924057798366491998077727991424922911165403434522806469328114407334094535810942859512352089785125683335350062474092708044674085769524387654467267128528564551803293661877480971961092735622606052503557881856409855812611523475975566606131897917979412576797874632169829901968854843162299366867885636535326810998541141840561418097240137120398317445832694001031827068485975315937269024666370665530455146256019590700349556357390218401217383173228376078058967743472704019765210324846681867991543267171763037513180046865961560351035005185946817643006206395175857900512245900162751815626427008481585714891\"},\"rctxt\":\"54359809198312125478916383106913469635175253891208897419510030559787479974126666313900084654632259260010008369569778456071591398552341004538623276997178295939490854663263886825856426285604332554317424030793691008221895556474599466123873279022389276698551452690414982831059651505731449763128921782866843113361548859434294057249048041670761184683271568216202174527891374770703485794299697663353847310928998125365841476766767508733046891626759537001358973715760759776149482147060701775948253839125589216812475133616408444838011643485797584321993661048373877626880635937563283836661934456534313802815974883441215836680800\",\"z\":\"99592262675748359673042256590146366586480829950402370244401571195191609039150608482506917768910598228167758026656953725016982562881531475875469671976107506976812319765644401707559997823702387678953647104105378063905395973550729717937712350758544336716556268064226491839700352305793370980462034813589488455836259737325502578253339820590260554457468082536249525493340350556649403477875367398139579018197084796440810685458274393317299082017275568964540311198115802021902455672385575542594821996060452628805634468222196284384514736044680778624637228114693554834388824212714580770066729185685978935409859595244639193538156\"}},\"issuerId\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550\"}"
}
