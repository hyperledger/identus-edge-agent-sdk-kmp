package io.iohk.atala.prism.walletsdk.castor

import io.iohk.atala.prism.apollo.ApolloMock
import io.iohk.atala.prism.walletsdk.castor.did.DIDParser
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DIDSignatureVerificationTest {

    @Test
    fun testCastorVerifySignature_Should_return_true_when_correctKeysAreUsed() = runTest {
        val verKeyStr = "z6LSci5EK4Ezue5QA72ZX71QUbXY2xr5ygRw7wM1WJigTNnd"
        val authKeyStr = "z6MkqgCXHEGr2wJZANPZGC8WFmeVuS3abAD9uvh7mTXygCFv"
        val serviceStr = "eyJpZCI6IkRJRENvbW1WMiIsInQiOiJkbSIsInMiOiJsb2NhbGhvc3Q6ODA4MiIsInIiOltdLCJhIjpbImRtIl19"
        val didString = "did:peer:2.E$verKeyStr.V$authKeyStr.S$serviceStr"

        val castor = io.iohk.atala.prism.walletsdk.castor.CastorImpl(ApolloMock())

        val did = DIDParser.parse(didString)
        val result = castor.verifySignature(did, "".encodeToByteArray(), "".encodeToByteArray())
        assertEquals(true, result)
    }

    @Test
    fun testCastorVerifySignature_Should_return_error_when_incorrectKeysAreUsed() = runTest {
        val verKeyStr = "z6LSci5EK4Ezue5QA72ZX71QUbXY2xr5ygRw7wM1WJigTNnd"
        val serviceStr = "eyJpZCI6IkRJRENvbW1WMiIsInQiOiJkbSIsInMiOiJsb2NhbGhvc3Q6ODA4MiIsInIiOltdLCJhIjpbImRtIl19"
        val didString = "did:peer:2.E$verKeyStr.V$verKeyStr.S$serviceStr"

        val castor = io.iohk.atala.prism.walletsdk.castor.CastorImpl(ApolloMock())

        val did = DIDParser.parse(didString)

        assertFailsWith<CastorError.InvalidPeerDIDError> {
            castor.verifySignature(did, "".encodeToByteArray(), "".encodeToByteArray())
        }
    }
}
