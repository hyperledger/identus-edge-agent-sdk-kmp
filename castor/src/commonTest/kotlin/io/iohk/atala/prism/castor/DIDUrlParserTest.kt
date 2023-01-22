package io.iohk.atala.prism.castor

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DIDUrlParserTest {

    @Test
    fun it_should_test_valid_Urls() {
        var didExample1 = "did:example:123456:adsd/path?query=something#fragment"
        var didExample2 = "did:example:123456/path?query=something&query2=something#0"
        var didExample3 = "did:example:123456/path/jpg.pp?query=something"

        var parsedDID1 =  DIDUrlParser(didExample1).parse()
        var parsedDID2 =  DIDUrlParser(didExample2).parse()
        var parsedDID3 =  DIDUrlParser.parse(didExample3)

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
        assertContentEquals(parsedDID3.path, arrayOf("path",  "jpg.pp"))
        assertEquals(parsedDID3.parameters, mapOf("query" to "something"))
        assertNull(parsedDID3.fragment)
    }

}
