package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.PrismAgentError
import io.iohk.atala.prism.domain.models.Seed
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PrismAgentTests {

    @Test
    fun testCreateNewPrismDID_shouldCreateNewDID_whenCalled() = runTest {
        val apollo = ApolloMock()
        val castor = CastorMock()
        val pluto = PlutoMock()
        val seed = Seed(ByteArray(0))
        val validDID = DID("did", "test", "123")
        castor.createPrismDIDReturn = validDID
        val agent = PrismAgent(
            apollo = apollo,
            castor = castor,
            pluto = pluto,
            seed = seed
        )
        val newDID = agent.createNewPrismDID()
        assertEquals(newDID, validDID)
        assertEquals(newDID, pluto.storedPrismDID.first())
    }

    @Test
    fun testCreateNewPeerDID_shouldCreateNewDID_whenCalled() = runTest {
        val apolloMock = ApolloMock()
        val castorMock = CastorMock()
        val plutoMock = PlutoMock()
        val validDID = DID("did", "test", "123")
        castorMock.createPeerDIDReturn = validDID
        val agent = PrismAgent(apolloMock, castorMock, plutoMock)
        val newDID = agent.createNewPeerDID(services = emptyArray(), updateMediator = false)

        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMock.storedPeerDID.first())
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldAcceptOnboardingInvitation_whenStatusIs200() = runTest {
        val apolloMock = ApolloMock()
        val castorMock = CastorMock()
        val plutoMock = PlutoMock()
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
        val apolloMock = ApolloMock()
        val castorMock = CastorMock()
        val plutoMock = PlutoMock()
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
        val apolloMock = ApolloMock()
        val castorMock = CastorMock()
        val plutoMock = PlutoMock()
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
}
