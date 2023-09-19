package io.iohk.atala.prism.walletsdk.prismagent

/* ktlint-disable import-ordering */
import io.iohk.atala.prism.walletsdk.apollo.ApolloImpl
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PrivateKey
import io.iohk.atala.prism.walletsdk.castor.CastorImpl
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import io.iohk.atala.prism.walletsdk.logger.PrismLoggerMock
import io.iohk.atala.prism.walletsdk.mercury.ApiMock
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.PrismOnboardingInvitation
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/* ktlint-disable import-ordering */

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PrismAgentTests {

    lateinit var apolloMock: ApolloMock
    lateinit var castorMock: CastorMock
    lateinit var plutoMock: PlutoMock
    lateinit var mercuryMock: MercuryMock
    lateinit var polluxMock: PolluxMock
    lateinit var mediationHandlerMock: MediationHandlerMock
    lateinit var connectionManager: ConnectionManager

    @BeforeTest
    fun setup() {
        apolloMock = ApolloMock()
        castorMock = CastorMock()
        plutoMock = PlutoMock()
        mercuryMock = MercuryMock()
        polluxMock = PolluxMock()
        mediationHandlerMock = MediationHandlerMock()
        // Pairing will be removed in the future
        connectionManager = ConnectionManager(mercuryMock, castorMock, plutoMock, mediationHandlerMock, mutableListOf())
    }

    @Test
    fun testCreateNewPrismDID_shouldCreateNewDID_whenCalled() = runTest {
        val seed = Seed(ByteArray(0))
        val validDID = DID("did", "test", "123")
        castorMock.createPrismDIDReturn = validDID
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = seed,
            api = null,
            logger = PrismLoggerMock()
        )
        plutoMock.getPrismLastKeyPathIndexReturn = flow { emit(0) }
        val newDID = agent.createNewPrismDID()
        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMock.storedPrismDID.first())
        assertTrue { plutoMock.wasGetPrismLastKeyPathIndexCalled }
        assertTrue { plutoMock.wasStorePrismDIDAndPrivateKeysCalled }
    }

    @Test
    fun testCreateNewPeerDID_shouldCreateNewDID_whenCalled() = runTest {
        val validDID = DID("did", "test", "123")
        castorMock.createPeerDIDReturn = validDID
        val agent = PrismAgent(
            apolloMock,
            castorMock,
            plutoMock,
            mercuryMock,
            polluxMock,
            connectionManager,
            null,
            null,
            logger = PrismLoggerMock()
        )

        val newDID = agent.createNewPeerDID(services = emptyArray(), updateMediator = false)

        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMock.storedPeerDID.first())
        assertTrue { plutoMock.wasStorePeerDIDAndPrivateKeysCalled }
    }

    @Test
    fun testCreateNewPeerDID_whenUpdateMediatorFalse_thenShouldUseProvidedServices() = runTest {
        val apollo = ApolloImpl()
        val castor = CastorImpl(apollo = apollo, logger = PrismLoggerMock())
        val agent = PrismAgent(
            apollo,
            castor,
            plutoMock,
            mercuryMock,
            polluxMock,
            connectionManager,
            null,
            null,
            logger = PrismLoggerMock()
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
    fun testPrismAgentOnboardingInvitation_shouldAcceptOnboardingInvitation_whenStatusIs200() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
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
    fun testPrismAgentOnboardingInvitation_shouldRejectOnboardingInvitation_whenStatusIsNot200() = runTest {
        val api = ApiMock(HttpStatusCode.BadRequest, "{\"success\":\"true\"}")
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
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
        assertFailsWith<PrismAgentError.FailedToOnboardError> {
            agent.acceptInvitation(invitation as PrismOnboardingInvitation)
        }
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldRejectOnboardingInvitation_whenBodyIsWrong() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
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
        assertFailsWith<PrismAgentError.UnknownInvitationTypeError> {
            agent.parseInvitation(invitationString)
        }
    }

    @Test
    fun testPrismAgentSignWith_whenNoPrivateKeyAvailable_thenThrowCannotFindDIDPrivateKey() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )

        plutoMock.getDIDPrivateKeysReturn = flow { emit(listOf(null)) }

        val did = DID("did", "peer", "asdf1234asdf1234")
        val messageString = "This is a message"
        assertFalse { plutoMock.wasGetDIDPrivateKeysByDIDCalled }
        assertFailsWith(PrismAgentError.CannotFindDIDPrivateKey::class, null) {
            agent.signWith(did, messageString.toByteArray())
        }
    }

    @Test
    fun testPrismAgentSignWith_whenPrivateKeyAvailable_thenSignatureReturned() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )

        val privateKeys = listOf(Secp256k1PrivateKey(byteArrayOf()))
        plutoMock.getDIDPrivateKeysReturn = flow { emit(privateKeys) }

        val did = DID("did", "peer", "asdf1234asdf1234")
        val messageString = "This is a message"

        assertEquals(Signature::class, agent.signWith(did, messageString.toByteArray())::class)
        assertTrue { plutoMock.wasGetDIDPrivateKeysByDIDCalled }
    }

    @Test
    fun testParseInvitation_whenOutOfBand_thenReturnsOutOfBandInvitationObject() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
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
    fun testParseInvitation_whenOutOfBandWrongBody_thenThrowsUnknownInvitationTypeError() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
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

        assertFailsWith<PrismAgentError.UnknownInvitationTypeError> {
            agent.parseInvitation(invitationString.trim())
        }
    }

    @Test
    fun testStartPrismAgent_whenCalled_thenStatusIsRunning() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )
        assertEquals(PrismAgent.State.STOPPED, agent.state)
        agent.start()
        assertEquals(PrismAgent.State.RUNNING, agent.state)
    }

    @Test
    fun testStopPrismAgent_whenCalled_thenStatusIsStopped() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )
        agent.stop()
        assertEquals(PrismAgent.State.STOPPED, agent.state)
    }

    @Test
    fun test_OOPInvitationInURLFormat() = runTest {
        val oob =
            "https://my.domain.com/path?_oob=eyJpZCI6ImQzNjM3NzlhLWYyMmItNGFiNC1hYjY0LTkxZjkxNjgzNzYwNyIsInR5cGUiOiJodHRwczovL2RpZGNvbW0ub3JnL291dC1vZi1iYW5kLzIuMC9pbnZpdGF0aW9uIiwiZnJvbSI6ImRpZDpwZWVyOjIuRXo2TFNjcGZReGJ2VEhLaGpvbzVvMzlmc254VEp1RTRobVp3ckROUE5BVzI0dmFORi5WejZNa3UzSkpVTDNkaHpYQXB0RWpuUDFpNkF0TDlTNGlwRTNYOHM3MWV4MW9WVGNHLlNleUowSWpvaVpHMGlMQ0p6SWpvaWFIUjBjSE02THk5ck9ITXRaR1YyTG1GMFlXeGhjSEpwYzIwdWFXOHZjSEpwYzIwdFlXZGxiblF2Wkdsa1kyOXRiU0lzSW5JaU9sdGRMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDAiLCJib2R5Ijp7ImdvYWxfY29kZSI6ImlvLmF0YWxhcHJpc20uY29ubmVjdCIsImdvYWwiOiJFc3RhYmxpc2ggYSB0cnVzdCBjb25uZWN0aW9uIGJldHdlZW4gdHdvIHBlZXJzIHVzaW5nIHRoZSBwcm90b2NvbCAnaHR0cHM6Ly9hdGFsYXByaXNtLmlvL21lcmN1cnkvY29ubmVjdGlvbnMvMS4wL3JlcXVlc3QnIiwiYWNjZXB0IjpbXX19"
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )
        val x = agent.parseInvitation(oob)
        assert(x is OutOfBandInvitation)
        assert((x as OutOfBandInvitation).type == ProtocolType.Didcomminvitation)
    }
}
