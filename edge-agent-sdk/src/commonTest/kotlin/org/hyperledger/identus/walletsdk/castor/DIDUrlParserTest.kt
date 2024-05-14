package org.hyperledger.identus.walletsdk.castor

import org.hyperledger.identus.walletsdk.castor.did.DIDUrlParser
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DIDUrlParserTest {

    @Test
    fun it_should_test_valid_Urls() {
        val didExample1 = "did:example:123456:adsd/path?query=something#fragment"
        val didExample2 = "did:example:123456/path?query=something&query2=something#0"
        val didExample3 = "did:example:123456/path/jpg.pp?query=something"

        val parsedDID1 = DIDUrlParser.parse(didExample1)
        val parsedDID2 = DIDUrlParser.parse(didExample2)
        val parsedDID3 = DIDUrlParser.parse(didExample3)

        assertEquals(parsedDID1.did.schema, "did")
        assertEquals(parsedDID1.did.method, "example")
        assertEquals(parsedDID1.did.methodId, "123456:adsd")
        assertContentEquals(parsedDID1.path, arrayOf("path"))
        assertEquals(parsedDID1.parameters, mapOf("query" to "something"))
        assertEquals(parsedDID1.fragment, "fragment")

        assertEquals(parsedDID2.did.schema, "did")
        assertEquals(parsedDID2.did.method, "example")
        assertEquals(parsedDID2.did.methodId, "123456")
        assertContentEquals(parsedDID2.path, arrayOf("path"))
        assertEquals(parsedDID2.parameters, mapOf("query" to "something", "query2" to "something"))
        assertEquals(parsedDID2.fragment, "0")

        assertEquals(parsedDID3.did.schema, "did")
        assertEquals(parsedDID3.did.method, "example")
        assertEquals(parsedDID3.did.methodId, "123456")
        assertContentEquals(parsedDID3.path, arrayOf("path", "jpg.pp"))
        assertEquals(parsedDID3.parameters, mapOf("query" to "something"))
        assertNull(parsedDID3.fragment)
    }
}
