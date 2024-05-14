package org.hyperledger.identus.walletsdk.pollux

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.hyperledger.identus.walletsdk.mercury.ApiMock
import org.hyperledger.identus.walletsdk.prismagent.CastorMock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PolluxImplTest {

    lateinit var pollux: PolluxImpl
    lateinit var castorMock: CastorMock
    lateinit var apiMock: ApiMock

    @BeforeTest
    fun setup() {
        castorMock = CastorMock()
        val json =
            "{\"name\":\"Schema name\",\"version\":\"1.1\",\"attrNames\":[\"name\",\"surname\"],\"issuerId\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550\"}"
        apiMock = ApiMock(HttpStatusCode.OK, json)
        pollux = PolluxImpl(castorMock, apiMock)
    }

    @Test
    fun testGetSchema_whenAnoncred_thenSchemaCorrect() = runTest {
        val schema = pollux.getSchema("")
        val attrNames = listOf("name", "surname")
        assertEquals("Schema name", schema.name)
        assertEquals("1.1", schema.version)
        assertEquals(attrNames, schema.attrNames)
        assertEquals("did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550", schema.issuerId)
    }
}
