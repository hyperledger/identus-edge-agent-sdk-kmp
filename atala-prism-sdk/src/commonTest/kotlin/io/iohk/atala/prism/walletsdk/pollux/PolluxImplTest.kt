package io.iohk.atala.prism.walletsdk.pollux

import io.iohk.atala.prism.walletsdk.mercury.ApiMock
import io.iohk.atala.prism.walletsdk.prismagent.CastorMock
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
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
            "{\"guid\":\"39a47736-2ecc-3250-8e3c-588c154bb927\",\"id\":\"c33d6528-ea4d-4b9b-9974-22421bd14161\",\"longId\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550/c33d6528-ea4d-4b9b-9974-22421bd14161?version=2.0.0\",\"name\":\"automation-anoncred\",\"version\":\"2.0.0\",\"tags\":[\"automation\"],\"description\":\"anoncred test\",\"type\":\"AnoncredSchemaV1\",\"schema\":{\"name\":\"Schema name\",\"version\":\"1.1\",\"attrNames\":[\"name\",\"surname\"],\"issuerId\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550\"},\"author\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550\",\"authored\":\"2023-12-04T14:57:35.282084Z\",\"kind\":\"CredentialSchema\",\"self\":\"/schema-registry/schemas/39a47736-2ecc-3250-8e3c-588c154bb927\"}"
        apiMock = ApiMock(HttpStatusCode.OK, json)
        pollux = PolluxImpl(castorMock, apiMock)
    }

    @Test
    fun testGetSchema_whenAnoncred_thenSchemaCorrect() = runTest {
        val schema = pollux.getSchema("")
        val attrNames = listOf("name", "surname")
        assertEquals("automation-anoncred", schema.name)
        assertEquals("2.0.0", schema.version)
        assertEquals(attrNames, schema.attrNames)
        assertEquals("did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550", schema.issuerId)
    }
}
