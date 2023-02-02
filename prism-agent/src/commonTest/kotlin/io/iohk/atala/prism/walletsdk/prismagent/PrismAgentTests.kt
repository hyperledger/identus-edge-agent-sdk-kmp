package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.domain.models.Curve
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.KeyCurve
import io.iohk.atala.prism.domain.models.PrismAgentError
import io.iohk.atala.prism.domain.models.PrivateKey
import io.iohk.atala.prism.domain.models.Seed
import io.iohk.atala.prism.domain.models.Signature
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

    @BeforeTest
    fun setup() {
        apolloMock = ApolloMock()
        castorMock = CastorMock()
        plutoMock = PlutoMock()
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
            seed = seed
        )
        val newDID = agent.createNewPrismDID()
        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMock.storedPrismDID.first())
        assertTrue { plutoMock.wasGetPrismLastKeyPathIndexCalled }
        assertTrue { plutoMock.wasStorePrivateKeysCalled }
        assertTrue { plutoMock.wasStorePrismDIDCalled }
    }

    @Test
    fun testCreateNewPeerDID_shouldCreateNewDID_whenCalled() = runTest {
        val validDID = DID("did", "test", "123")
        castorMock.createPeerDIDReturn = validDID
        val agent = PrismAgent(apolloMock, castorMock, plutoMock)
        val newDID = agent.createNewPeerDID(services = emptyArray(), updateMediator = false)

        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMock.storedPeerDID.first())
        assertTrue { plutoMock.wasStorePeerDIDCalled }
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldAcceptOnboardingInvitation_whenStatusIs200() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}")
        )
        var invitationString = """
            {
                "type":"Onboarding",
                "onboardEndpoint":"http://localhost/onboarding",
                "from":"did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
            }
        """
        val invitation = agent.parseInvitation(invitationString)
        agent.acceptInvitation(invitation as PrismAgent.PrismOnboardingInvitation)
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldRejectOnboardingInvitation_whenStatusIsNot200() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            api = ApiMock(HttpStatusCode.BadRequest, "{\"success\":\"true\"}")
        )
        var invitationString = """
            {
                "type":"Onboarding",
                "onboardEndpoint":"http://localhost/onboarding",
                "from":"did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
            }
        """
        val invitation = agent.parseInvitation(invitationString)
        assertFailsWith<PrismAgentError.failedToOnboardError> {
            agent.acceptInvitation(invitation as PrismAgent.PrismOnboardingInvitation)
        }
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldRejectOnboardingInvitation_whenBodyIsWrong() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}")
        )
        var invitationString = """
            {
                "type":"Onboarding",
                "errorField":"http://localhost/onboarding",
                "from":"did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
            }
        """
        assertFailsWith<PrismAgentError.unknownInvitationTypeError> {
            agent.parseInvitation(invitationString)
        }
    }

    @Test
    fun testPrismAgentSignWith_whenNoPrivateKeyAvailable_thenThrowCannotFindDIDPrivateKey() = runTest {
        val agent = PrismAgent(
            apolloMock,
            castorMock,
            plutoMock
        )

        plutoMock.getDIDPrivateKeysReturn = flow {
            emit(null)
        }

        val did = DID("did", "peer", "asdf1234asdf1234")
        val messageString = "This is a message"
        assertFalse { plutoMock.wasGetDIDPrivateKeysByDIDCalled}
        assertFailsWith(PrismAgentError.cannotFindDIDPrivateKey::class, null) {
            agent.signWith(did, messageString.toByteArray())
        }
    }

    @Test
    fun testPrismAgentSignWith_whenPrivateKeyAvailable_thenSignatureReturned() = runTest {
        val agent = PrismAgent(
            apolloMock,
            castorMock,
            plutoMock
        )

        plutoMock.getDIDPrivateKeysReturn = flow {
            val privateKeys = arrayOf(PrivateKey(KeyCurve(Curve.SECP256K1), byteArrayOf()))
            emit(privateKeys)
        }

        val did = DID("did", "peer", "asdf1234asdf1234")
        val messageString = "This is a message"

        assertEquals(Signature::class, agent.signWith(did, messageString.toByteArray())::class)
        assertTrue { plutoMock.wasGetDIDPrivateKeysByDIDCalled}
    }
}
