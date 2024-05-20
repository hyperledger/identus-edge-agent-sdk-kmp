package org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand

import io.ktor.http.Url
import org.hyperledger.identus.walletsdk.domain.models.CommonError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OutOfBandParserTest {

    @Test
    fun testParseMessage_whenCorrectUrlPassed_thenResultJsonCorrect() {
        val expectedJson =
            "{\"body\":\"{}\",\"id\":\"a38abd0d-de3e-4d3f-a4ac-a41ef58ac3fc\",\"piuri\":\"Something wrong\",\"attachments\":[],\"extraHeaders\":[],\"createdTime\":\"1678471374846\",\"expiresTimePlus\":\"167847137484686400000\",\"ack\":[],\"direction\":1}"
        val resultJson =
            OutOfBandParser().parseMessage(Url("localhost:8080?_oob=eyJib2R5Ijoie30iLCJpZCI6ImEzOGFiZDBkLWRlM2UtNGQzZi1hNGFjLWE0MWVmNThhYzNmYyIsInBpdXJpIjoiU29tZXRoaW5nIHdyb25nIiwiYXR0YWNobWVudHMiOltdLCJleHRyYUhlYWRlcnMiOltdLCJjcmVhdGVkVGltZSI6IjE2Nzg0NzEzNzQ4NDYiLCJleHBpcmVzVGltZVBsdXMiOiIxNjc4NDcxMzc0ODQ2ODY0MDAwMDAiLCJhY2siOltdLCJkaXJlY3Rpb24iOjF9"))
        assertEquals(expectedJson, resultJson)
    }

    @Test
    fun testParseMessage_whenWrongUrlPassed_thenThrowsInvalidURLError() {
        assertFailsWith(CommonError.InvalidURLError::class) {
            OutOfBandParser().parseMessage(Url("localhost:8080?_oobb=eyJib2R5Ijoie3"))
        }
    }
}
