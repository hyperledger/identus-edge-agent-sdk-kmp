package org.hyperledger.identus.walletsdk.prismagent.protocols.issueCredential

import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.prismagent.PrismAgentError
import org.hyperledger.identus.walletsdk.prismagent.helpers.fromIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OfferCredentialTest {

    @Test
    fun testWhenValidOfferMessageThenInitOfferCredential() {
        val fromDID = DID.fromIndex(index = 0)
        val toDID = DID.fromIndex(index = 1)
        val validOfferCredential = OfferCredential(
            body = OfferCredential.Body(
                credentialPreview = CredentialPreview(
                    attributes = arrayOf(
                        CredentialPreview.Attribute(
                            name = "test1",
                            value = "test",
                            mediaType = "test.x"
                        )
                    )
                )
//                formats = arrayOf(
//                    CredentialFormat(
//                        attachId = "test1",
//                        format = "test"
//                    )
//                )
            ),
            attachments = arrayOf(),
            thid = "1",
            from = fromDID,
            to = toDID
        )
        val offerMessage = validOfferCredential.makeMessage()
        val testOfferCredentialFormat = OfferCredential.fromMessage(offerMessage)
        assertEquals(testOfferCredentialFormat, validOfferCredential)
    }

    @Test
    fun testWhenInvalidOfferMessageThenInitOfferCredential() {
        val invalidOfferCredential = Message(
            piuri = "InvalidType",
            from = null,
            to = null,
            body = ""
        )
        assertFailsWith<PrismAgentError.InvalidMessageType> {
            OfferCredential.fromMessage(invalidOfferCredential)
        }
    }
}
