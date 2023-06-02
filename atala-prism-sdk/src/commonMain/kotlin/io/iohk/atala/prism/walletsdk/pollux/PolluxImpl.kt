package io.iohk.atala.prism.walletsdk.pollux

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.apollo.utils.KMMEllipticCurve
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.JWTCredentialPayload
import io.iohk.atala.prism.walletsdk.domain.models.JsonString
import io.iohk.atala.prism.walletsdk.domain.models.PolluxError
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.ECParameterSpec
import java.security.spec.ECPrivateKeySpec

class PolluxImpl(val castor: Castor) : Pollux {

    @Throws(PolluxError.InvalidJWTString::class, PolluxError.InvalidCredentialError::class)
    override fun parseVerifiableCredential(jwtString: String): VerifiableCredential {
        val jwtParts = jwtString.split(JWT_DELIMITER)
        if (jwtParts.size != JWT_PARTS_SIZE) {
            throw PolluxError.InvalidJWTString()
        }
        val decodedBase64CredentialJson: JsonString = try {
            jwtParts[JWT_SECOND_PART].base64UrlDecoded
        } catch (e: Throwable) {
            e.printStackTrace()
            throw PolluxError.InvalidCredentialError()
        }

        val verifiableCredentialJson = Json.decodeFromString<JWTJsonPayload>(decodedBase64CredentialJson)

        return JWTCredentialPayload(
            iss = DID(verifiableCredentialJson.iss),
            exp = verifiableCredentialJson.exp.toString(),
            nbf = verifiableCredentialJson.nbf.toString(),
            jti = jwtString,
            verifiableCredential = JWTCredentialPayload.JWTVerifiableCredential(
                credentialType = CredentialType.JWT,
                issuer = DID(verifiableCredentialJson.iss),
                credentialSubject = verifiableCredentialJson.sub,
                id = jwtString,
                issuanceDate = verifiableCredentialJson.nbf.toString(),
                expirationDate = verifiableCredentialJson.exp.toString(),
                proof = null
            ),
            sub = verifiableCredentialJson.sub
        )
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
        val curveName = KMMEllipticCurve.SECP256k1.value
        val sp = ECNamedCurveTable.getParameterSpec(curveName)
        val params: ECParameterSpec = ECNamedCurveSpec(sp.name, sp.curve, sp.g, sp.n, sp.h)
        val privateKeySpec = ECPrivateKeySpec(BigInteger(1, privateKey.value), params)
        val keyFactory = KeyFactory.getInstance(EC, BouncyCastleProvider())
        return keyFactory.generatePrivate(privateKeySpec) as ECPrivateKey
    }

    private fun getDomain(jsonObject: JsonObject): String? {
        return jsonObject[OPTIONS]?.jsonObject?.get(DOMAIN)?.jsonPrimitive?.content
    }

    private fun getChallenge(jsonObject: JsonObject): String? {
        return jsonObject[OPTIONS]?.jsonObject?.get(CHALLENGE)?.jsonPrimitive?.content
    }

    private fun signClaimsRequestCredentialJWT(
        subjectDID: DID,
        privateKey: ECPrivateKey,
        domain: String,
        challenge: String
    ): String {
        // Define the JWT claims
        val vp = mapOf(
            CONTEXT to setOf(CONTEXT_URL),
            TYPE to setOf(VERIFIABLE_PRESENTATION)
        )
        val claims = JWTClaimsSet.Builder()
            .issuer(subjectDID.toString())
            .audience(domain)
            .claim(NONCE, challenge)
            .claim(VP, vp)
            .build()

        // Generate a JWS header with the ES256K algorithm
        val header = JWSHeader.Builder(JWSAlgorithm.ES256K)
            .build()

        // Sign the JWT with the private key
        val jwsObject = SignedJWT(header, claims)
        val signer = ECDSASigner(privateKey as java.security.PrivateKey, com.nimbusds.jose.jwk.Curve.SECP256K1)
        val provider = BouncyCastleProviderSingleton.getInstance()
        signer.jcaContext.provider = provider
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
            CONTEXT to setOf(CONTEXT_URL),
            TYPE to setOf(VERIFIABLE_PRESENTATION),
            VERIFIABLE_CREDENTIAL to listOf(credential.id)
        )
        val claims = JWTClaimsSet.Builder()
            .audience(domain)
            .issuer(subjectDID.toString())
            .claim(NONCE, challenge)
            .claim(VP, vp)
            .build()

        // Generate a JWS header with the ES256K algorithm
        val header = JWSHeader.Builder(JWSAlgorithm.ES256K)
            .build()

        // Sign the JWT with the private key
        val jwsObject = SignedJWT(header, claims)
        val signer = ECDSASigner(privateKey as java.security.PrivateKey, com.nimbusds.jose.jwk.Curve.SECP256K1)
        val provider = BouncyCastleProviderSingleton.getInstance()
        signer.jcaContext.provider = provider
        jwsObject.sign(signer)

        // Serialize the JWS object to a string
        return jwsObject.serialize()
    }
}
