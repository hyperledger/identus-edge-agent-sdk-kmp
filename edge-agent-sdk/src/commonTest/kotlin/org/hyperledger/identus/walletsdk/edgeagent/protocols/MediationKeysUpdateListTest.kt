package org.hyperledger.identus.walletsdk.edgeagent.protocols

import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.edgeagent.protocols.mediation.MediationKeysUpdateList
import kotlin.test.Test
import kotlin.test.assertEquals

class MediationKeysUpdateListTest {
    @Test
    fun makeMessageShouldReturnValidMessage() {
        // given
        val id = "test_id"
        val from = DID("did:example:123")
        val to = DID("did:example:456")
        val recipientDid = DID("did:example:789")
        val mediationKeysUpdateList = MediationKeysUpdateList(id, from, to, arrayOf(recipientDid))

        // when
        val message = mediationKeysUpdateList.makeMessage()

        // then
        assertEquals(id, message.id)
        assertEquals(ProtocolType.DidcommMediationKeysUpdate.value, message.piuri)
        assertEquals(from, message.from)
        assertEquals(to, message.to)

        val expectedBody = MediationKeysUpdateList.Body(
            updates = arrayOf(MediationKeysUpdateList.Update(recipientDid = recipientDid.toString()))
        )
        val actualBody: MediationKeysUpdateList.Body = Json.decodeFromString(message.body)
        assertEquals(expectedBody, actualBody)
    }
}
