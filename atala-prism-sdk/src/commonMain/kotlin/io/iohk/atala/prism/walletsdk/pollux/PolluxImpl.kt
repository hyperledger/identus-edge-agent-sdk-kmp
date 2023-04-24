package io.iohk.atala.prism.walletsdk.pollux

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.JsonString
import io.iohk.atala.prism.walletsdk.domain.models.PolluxError
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import io.iohk.atala.prism.walletsdk.domain.models.W3CVerifiableCredential
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import kotlin.jvm.Throws

class PolluxImpl(val castor: Castor) : Pollux {

    @Throws(PolluxError.InvalidJWTString::class, PolluxError.InvalidCredentialError::class)
    override fun parseVerifiableCredential(jwtString: String): VerifiableCredential {
        val jwtParts = jwtString.split(".")
        if (jwtParts.size != 3) {
            throw PolluxError.InvalidJWTString()
        }
        val decodedBase64CredentialJson: JsonString = try {
            jwtParts.first().base64UrlDecoded
        } catch (e: Throwable) {
            throw PolluxError.InvalidCredentialError(e.message)
        }

        val verifiableCredential = Json.decodeFromString<VerifiableCredential>(decodedBase64CredentialJson)

        return when (verifiableCredential.type.first()) {
            CredentialType.JWT.type -> {
                JWTCredential(
                    id = jwtString,
                    json = decodedBase64CredentialJson,
                ).makeVerifiableCredential()
            }
            CredentialType.W3C.type -> {
                Json.decodeFromString<W3CVerifiableCredential>(decodedBase64CredentialJson)
            }
            else -> throw PolluxError.InvalidCredentialError()
        }
    }

    @Throws(PolluxError.NoDomainOrChallengeFound::class)
    override fun createRequestCredentialJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        offerJson: JsonObject
    ): String {
        val parsedPrivateKey = parsePrivateKey(privateKey)
        val domain = getDomain(offerJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        val challenge = getChallenge(offerJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        return signClaimsRequestCredentialJWT(subjectDID, parsedPrivateKey, domain, challenge)
    }

    @Throws(PolluxError.NoDomainOrChallengeFound::class)
    override fun createVerifiablePresentationJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        credential: VerifiableCredential,
        requestPresentationJson: JsonObject
    ): String {
        val parsedPrivateKey = parsePrivateKey(privateKey)
        val domain = getDomain(requestPresentationJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        val challenge = getChallenge(requestPresentationJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        return signClaimsProofPresentationJWT(subjectDID, parsedPrivateKey, credential, domain, challenge)
    }

    private fun parsePrivateKey(privateKey: PrivateKey): ECPrivateKey {
        val curveName = "secp256k1"
        val ecSpec = ECNamedCurveTable.getParameterSpec(curveName)
        val privateKeySpec = ECPrivateKeySpec(BigInteger(1, privateKey.value), ecSpec)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePrivate(privateKeySpec) as ECPrivateKey
    }

    private fun getDomain(jsonObject: JsonObject): String? {
        return jsonObject["domain"]?.jsonPrimitive?.content
    }

    private fun getChallenge(jsonObject: JsonObject): String? {
        return jsonObject["challenge"]?.jsonPrimitive?.content
    }

    private fun signClaimsRequestCredentialJWT(
        subjectDID: DID,
        privateKey: ECPrivateKey,
        domain: String,
        challenge: String
    ): String {
        // Define the JWT claims
        val vp = mapOf(
            "@context" to setOf("https://www.w3.org/2018/credentials/v1"),
            "type" to setOf("VerifiablePresentation")
        )
        val claims = JWTClaimsSet.Builder()
            .issuer(subjectDID.toString())
            .audience(domain)
            .claim("nonce", challenge)
            .claim("vp", vp)
            .build()

        // Generate a JWS header with the ES256K algorithm
        val header = JWSHeader.Builder(JWSAlgorithm.ES256K)
            .build()

        // Sign the JWT with the private key
        val jwsObject = SignedJWT(header, claims)
        val signer = ECDSASigner(privateKey)
        jwsObject.sign(signer)

        // Serialize the JWS object to a string
        return jwsObject.serialize()
    }

    private fun signClaimsProofPresentationJWT(
        subjectDID: DID,
        privateKey: ECPrivateKey,
        credential: VerifiableCredential,
        domain: String,
        challenge: String
    ): String {
        // Define the JWT claims
        val vp = mapOf(
            "@context" to setOf("https://www.w3.org/2018/credentials/v1"),
            "type" to setOf("VerifiablePresentation"),
            "verifiableCredential" to listOf(credential.id)
        )
        val claims = JWTClaimsSet.Builder()
            .audience(domain)
            .subject(subjectDID.toString())
            .claim("nonce", challenge)
            .claim("vp", vp)
            .build()

        // Generate a JWS header with the ES256K algorithm
        val header = JWSHeader.Builder(JWSAlgorithm.ES256K)
            .build()

        // Sign the JWT with the private key
        val jwsObject = SignedJWT(header, claims)
        val signer = ECDSASigner(privateKey)
        jwsObject.sign(signer)

        // Serialize the JWS object to a string
        return jwsObject.serialize()
    }
}
