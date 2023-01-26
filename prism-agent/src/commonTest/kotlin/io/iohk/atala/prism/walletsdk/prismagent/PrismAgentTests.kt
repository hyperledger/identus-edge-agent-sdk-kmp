package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.Seed
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
