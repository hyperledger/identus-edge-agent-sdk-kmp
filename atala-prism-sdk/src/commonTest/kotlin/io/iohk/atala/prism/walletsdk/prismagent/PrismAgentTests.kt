package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import io.iohk.atala.prism.walletsdk.prismagent.models.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.models.PrismOnboardingInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PrismAgentTests {

    lateinit var apolloMock: ApolloMock
    lateinit var castorMock: CastorMock
    lateinit var plutoMock: PlutoMock
    lateinit var mercuryMock: MercuryMock
    lateinit var mediationHandlerMock: MediationHandlerMock
    lateinit var connectionManager: ConnectionManager

    @BeforeTest
    fun setup() {
        apolloMock = ApolloMock()
        castorMock = CastorMock()
        plutoMock = PlutoMock()
        mercuryMock = MercuryMock()
        mediationHandlerMock = MediationHandlerMock()
        // TODO: Someone Please check this one had to add => mutableListOf()
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
            connectionManager = connectionManager,
            seed = seed,
            api = null,
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
            connectionManager,
            null,
            null,
        )
        val newDID = agent.createNewPeerDID(services = emptyArray(), updateMediator = false)

        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMock.storedPeerDID.first())
        assertTrue { plutoMock.wasStorePeerDIDAndPrivateKeysCalled }
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldAcceptOnboardingInvitation_whenStatusIs200() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            connectionManager = connectionManager,
            seed = null,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
        )
        var invitationString = """
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
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            connectionManager = connectionManager,
            seed = null,
            api = ApiMock(HttpStatusCode.BadRequest, "{\"success\":\"true\"}"),
        )
        var invitationString = """
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
            connectionManager = connectionManager,
            seed = null,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
        )
        var invitationString = """
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
            connectionManager = connectionManager,
            seed = null,
            api = null,
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
            connectionManager = connectionManager,
            seed = null,
            api = null,
        )

        val privateKeys = listOf(PrivateKey(KeyCurve(Curve.SECP256K1), byteArrayOf()))
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
            connectionManager = connectionManager,
            seed = null,
            api = null,
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
        assertEquals(OutOfBandInvitation::class, invitation::class)
        val oobInvitation: OutOfBandInvitation = invitation as OutOfBandInvitation
        assertEquals("https://didcomm.org/out-of-band/2.0/invitation", oobInvitation.type)
        assertEquals(DID("did:peer:asdf42sf"), oobInvitation.from)
        assertEquals(
            OutOfBandInvitation.Body(
                "issue-vc",
                "To issue a Faber College Graduate credential",
                arrayOf("didcomm/v2", "didcomm/aip2;env=rfc587"),
            ),
            oobInvitation.body,
        )
    }

    @Test
    fun testParseInvitation_whenOutOfBandWrongBody_thenThrowsUnknownInvitationTypeError() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
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
            connectionManager = connectionManager,
            seed = null,
            api = null,
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
            connectionManager = connectionManager,
            seed = null,
            api = null,
        )
        agent.stop()
        assertEquals(PrismAgent.State.STOPPED, agent.state)
    }
}
