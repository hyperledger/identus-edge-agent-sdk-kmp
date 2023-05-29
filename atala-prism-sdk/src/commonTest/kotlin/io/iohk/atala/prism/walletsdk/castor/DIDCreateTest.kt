package io.iohk.atala.prism.walletsdk.castor

import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.prismagent.DIDCOMM_MESSAGING
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DIDCreateTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun it_should_create_peerDID_correctly() = runTest {
        val keyAgreementKeyPair = KeyPair(
            KeyCurve(Curve.X25519),
            PrivateKey(KeyCurve(Curve.X25519), byteArrayOf(-24, 18, -94, -98, 106, 49, -55, -55, -6, -66, 75, 73, -120, -96, 33, 28, -55, -109, 105, -67, 9, 28, 88, -98, 43, -4, -6, 75, 65, -68, 112, 92)),
            PublicKey(KeyCurve(Curve.X25519), byteArrayOf(-16, 16, 107, -82, -125, 83, 101, -94, -103, -112, 47, 117, -90, -100, 64, 77, 62, -4, 11, 101, -53, -64, -2, -77, -106, -16, -63, -54, -42, -59, 80, 23))
        )

        val authenticationKeyPair = KeyPair(
            KeyCurve(Curve.ED25519),
            PrivateKey(KeyCurve(Curve.ED25519), byteArrayOf(-68, 7, 65, -41, -60, -42, -9, 55, -113, -80, -86, -106, -40, 97, 81, -13, -22, -124, -58, -39, -64, -126, 75, 88, 126, 39, 51, -52, 121, -64, -5, 35)),
            PublicKey(KeyCurve(Curve.ED25519), byteArrayOf(-8, 10, 34, 65, -42, -19, -92, 1, -48, -59, -10, -99, -8, -88, -32, 119, 50, -15, -93, 56, -121, -51, 78, -93, 42, -86, -37, 20, 34, -94, 108, -68))
        )

        val keyPairs: Array<KeyPair> = arrayOf(keyAgreementKeyPair, authenticationKeyPair)

        val castor = CastorImpl(ApolloMock())
        val did = castor.createPeerDID(keyPairs, emptyArray())
        val validPeerDID = "did:peer:2.Ez6LSsqHXypzFPGA7RdCu2NHUf2cK8dAW1AdHa5JDCRnXQ2yk.Vz6Mkw9W7jaqZ7hF5bSKeSpnqNxhFTiVMy3aBZjyEhrMxUQAF"
        assertEquals(validPeerDID, did.toString())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun it_should_throw_errors_if_wrong_keys_are_provided() = runTest {
        val verKeyStr = "z6LSci5EK4Ezue5QA72ZX71QUbXY2xr5ygRw7wM1WJigTNnd"
        val authKeyStr = "z6MkqgCXHEGr2wJZANPZGC8WFmeVuS3abAD9uvh7mTXygCFv"

        val fakeVerPrivateKey = PrivateKey(KeyCurve(Curve.ED25519), "".encodeToByteArray())
        val fakeAuthPrivateKey = PrivateKey(KeyCurve(Curve.X25519), "".encodeToByteArray())

        val verificationPubKey = PublicKey(KeyCurve(Curve.ED25519), verKeyStr.encodeToByteArray())
        val authenticationPubKey = PublicKey(KeyCurve(Curve.X25519), authKeyStr.encodeToByteArray())

        val verificationKeyPair = KeyPair(KeyCurve(Curve.ED25519), fakeVerPrivateKey, verificationPubKey)
        val authenticationKeyPair = KeyPair(KeyCurve(Curve.ED25519), fakeAuthPrivateKey, authenticationPubKey)

        val keyPairs: Array<KeyPair> = arrayOf(verificationKeyPair, authenticationKeyPair)

        val service = DIDDocument.Service(
            id = "DIDCommV2",
            type = arrayOf(DIDCOMM_MESSAGING),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "localhost:8082",
                accept = arrayOf(DIDCOMM_MESSAGING),
                routingKeys = arrayOf()
            )
        )

        val castor = CastorImpl(ApolloMock())

        assertFailsWith<CastorError.InvalidKeyError> {
            castor.createPeerDID(keyPairs, arrayOf(service))
        }
    }
}
