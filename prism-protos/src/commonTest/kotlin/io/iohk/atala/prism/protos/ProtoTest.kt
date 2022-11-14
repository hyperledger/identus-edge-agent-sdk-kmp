package io.iohk.atala.prism.protos

import pbandk.decodeFromByteArray
import pbandk.encodeToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class ProtoTest {
    @Test
    fun testProtobufModelEncoding() {
        val request = GetBatchStateRequest(batchId = "123")
        val decodedRequest = GetBatchStateRequest.decodeFromByteArray(request.encodeToByteArray())
        assertEquals(request, decodedRequest)
    }
}
