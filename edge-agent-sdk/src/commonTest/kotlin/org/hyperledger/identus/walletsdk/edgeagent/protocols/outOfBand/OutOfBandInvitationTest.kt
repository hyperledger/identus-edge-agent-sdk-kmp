package org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class OutOfBandInvitationTest {

    @Test
    fun testOutOfBandInvitation_whenJsonProvided_thenOutOfBandInvitationCorrect() {
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
        val oob = Json.decodeFromString<OutOfBandInvitation>(invitationString)
        assertEquals("https://didcomm.org/out-of-band/2.0/invitation", oob.type.value)
        assertEquals("1234-1234-1234-1234", oob.id)
        assertEquals("did:peer:asdf42sf", oob.from)
        assertEquals("issue-vc", oob.body.goalCode)
        assertEquals("To issue a Faber College Graduate credential", oob.body.goal)
        assertEquals("didcomm/v2", oob.body.accept?.get(0))
        assertEquals("didcomm/aip2;env=rfc587", oob.body.accept?.get(1))
    }
}
