@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.castor

import kotlinx.coroutines.test.runTest
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.X25519KeyPair
import org.hyperledger.identus.walletsdk.domain.DIDCOMM_MESSAGING
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.RawKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.TypeKey
import kotlin.test.Test
import kotlin.test.assertEquals

class DIDCreateTest {

    // TODO: Uncomment and solve the failing tests
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Ignore("Ignore for now")
//    @Test
//    fun it_should_create_peerDID_correctly() = runTest {
//        val keyAgreementKeyPair = X25519KeyPair(
//            X25519PrivateKey(
//                byteArrayOf(
//                    -24,
//                    18,
//                    -94,
//                    -98,
//                    106,
//                    49,
//                    -55,
//                    -55,
//                    -6,
//                    -66,
//                    75,
//                    73,
//                    -120,
//                    -96,
//                    33,
//                    28,
//                    -55,
//                    -109,
//                    105,
//                    -67,
//                    9,
//                    28,
//                    88,
//                    -98,
//                    43,
//                    -4,
//                    -6,
//                    75,
//                    65,
//                    -68,
//                    112,
//                    92
//                )
//            ),
//            X25519PublicKey(
//                byteArrayOf(
//                    -16,
//                    16,
//                    107,
//                    -82,
//                    -125,
//                    83,
//                    101,
//                    -94,
//                    -103,
//                    -112,
//                    47,
//                    117,
//                    -90,
//                    -100,
//                    64,
//                    77,
//                    62,
//                    -4,
//                    11,
//                    101,
//                    -53,
//                    -64,
//                    -2,
//                    -77,
//                    -106,
//                    -16,
//                    -63,
//                    -54,
//                    -42,
//                    -59,
//                    80,
//                    23
//                )
//            )
//        )
//
//        val authenticationKeyPair = Ed25519KeyPair(
//            Ed25519PrivateKey(
//                byteArrayOf(
//                    -68,
//                    7,
//                    65,
//                    -41,
//                    -60,
//                    -42,
//                    -9,
//                    55,
//                    -113,
//                    -80,
//                    -86,
//                    -106,
//                    -40,
//                    97,
//                    81,
//                    -13,
//                    -22,
//                    -124,
//                    -58,
//                    -39,
//                    -64,
//                    -126,
//                    75,
//                    88,
//                    126,
//                    39,
//                    51,
//                    -52,
//                    121,
//                    -64,
//                    -5,
//                    35
//                )
//            ),
//            Ed25519PublicKey(
//                byteArrayOf(
//                    -8,
//                    10,
//                    34,
//                    65,
//                    -42,
//                    -19,
//                    -92,
//                    1,
//                    -48,
//                    -59,
//                    -10,
//                    -99,
//                    -8,
//                    -88,
//                    -32,
//                    119,
//                    50,
//                    -15,
//                    -93,
//                    56,
//                    -121,
//                    -51,
//                    78,
//                    -93,
//                    42,
//                    -86,
//                    -37,
//                    20,
//                    34,
//                    -94,
//                    108,
//                    -68
//                )
//            )
//        )
//
//        val keyPairs: Array<KeyPair> = arrayOf(keyAgreementKeyPair, authenticationKeyPair)
//
//        val castor = CastorImpl(ApolloMock())
//        val did = castor.createPeerDID(keyPairs, emptyArray())
//        val validPeerDID =
//            "did:peer:2.Ez6LSsqHXypzFPGA7RdCu2NHUf2cK8dAW1AdHa5JDCRnXQ2yk.Vz6Mkw9W7jaqZ7hF5bSKeSpnqNxhFTiVMy3aBZjyEhrMxUQAF"
//        assertEquals(validPeerDID, did.toString())
//    }

    @Test
    fun testPeerDIDCreation_whenServicesProvided_thenCreatedCorrectly() = runTest {
        val apollo = ApolloImpl()

        val properties: MutableMap<String, Any> = mutableMapOf()
        properties[TypeKey().property] = KeyTypes.Curve25519
        properties[RawKey().property] = "COd9Xhr-amD7fuswWId2706JBUY_tmjp9eiNEieJeEE".base64UrlDecodedBytes
        properties[CurveKey().property] = Curve.X25519.value
        val keyAgreementPrivateKey = apollo.createPrivateKey(properties)
        val keyAgreementKeyPair = X25519KeyPair(
            privateKey = keyAgreementPrivateKey,
            publicKey = keyAgreementPrivateKey.publicKey()
        )

        val properties2: MutableMap<String, Any> = mutableMapOf()
        properties2[TypeKey().property] = KeyTypes.EC
        properties2[RawKey().property] = "JLIJQ5jlkyqtGmtOth6yggJLLC0zuRhUPiBhd1-rGPs".base64UrlDecodedBytes
        properties2[CurveKey().property] = Curve.ED25519.value
        val authenticationPrivateKey = apollo.createPrivateKey(properties2)
        val authenticationKeyPair = Ed25519KeyPair(
            privateKey = authenticationPrivateKey,
            publicKey = authenticationPrivateKey.publicKey()
        )

        val keyPairs: Array<KeyPair> = arrayOf(keyAgreementKeyPair, authenticationKeyPair)

        val service = DIDDocument.Service(
            id = "didcomm",
            type = arrayOf(DIDCOMM_MESSAGING),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "https://example.com/endpoint",
                accept = emptyArray(),
                routingKeys = arrayOf("did:example:somemediator#somekey")
            )
        )

        val castor = CastorImpl(apollo)
        val did = castor.createPeerDID(keyPairs, arrayOf(service))
        val expectedPeerDID =
            "did:peer:2.Ez6LSoHkfN1Y4nK9RCjx7vopWsLrMGNFNgTNZgoCNQrTzmb1n.Vz6MknRZmapV7uYZQuZez9n9N3tQotjRN18UGS68Vcfo6gR4h.SeyJ0IjoiZG0iLCJzIjp7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vZW5kcG9pbnQiLCJyIjpbImRpZDpleGFtcGxlOnNvbWVtZWRpYXRvciNzb21la2V5Il0sImEiOltdfX0"
        assertEquals(expectedPeerDID, did.toString())
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun it_should_throw_errors_if_wrong_keys_are_provided() = runTest {
//        val verKeyStr = "z6LSci5EK4Ezue5QA72ZX71QUbXY2xr5ygRw7wM1WJigTNnd"
//        val authKeyStr = "z6MkqgCXHEGr2wJZANPZGC8WFmeVuS3abAD9uvh7mTXygCFv"
//
//        val fakeVerPrivateKey = Ed25519PrivateKey("".encodeToByteArray())
//        val fakeAuthPrivateKey = X25519PrivateKey(ByteArray(32))
//
//        val verificationPubKey = Ed25519PublicKey(verKeyStr.encodeToByteArray())
//        val authenticationPubKey = X25519PublicKey(authKeyStr.encodeToByteArray())
//
//        val verificationKeyPair = Ed25519KeyPair(fakeVerPrivateKey, verificationPubKey)
//        val authenticationKeyPair = Ed25519KeyPair(fakeAuthPrivateKey, authenticationPubKey)
//
//        val keyPairs: Array<KeyPair> = arrayOf(verificationKeyPair, authenticationKeyPair)
//
//        val service = DIDDocument.Service(
//            id = "DIDCommV2",
//            type = arrayOf(DIDCOMM_MESSAGING),
//            serviceEndpoint = DIDDocument.ServiceEndpoint(
//                uri = "localhost:8082",
//                accept = arrayOf(DIDCOMM_MESSAGING),
//                routingKeys = arrayOf()
//            )
//        )
//
//        val castor = CastorImpl(ApolloMock())
//
//        assertFailsWith<CastorError.InvalidKeyError> {
//            castor.createPeerDID(keyPairs, arrayOf(service))
//        }
//    }
}
