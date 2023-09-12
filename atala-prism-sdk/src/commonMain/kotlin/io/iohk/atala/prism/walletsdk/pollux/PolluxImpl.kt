package io.iohk.atala.prism.walletsdk.pollux

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.iohk.atala.prism.apollo.utils.KMMEllipticCurve
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.PolluxError
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.domain.models.W3CCredential
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
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
    override fun parseVerifiableCredential(data: String): Credential {
        return try {
            JWTCredential(data)
        } catch (e: Exception) {
            try {
                Json.decodeFromString<W3CCredential>(data)
            } catch (e: Exception) {
                throw PolluxError.InvalidCredentialError(cause = e.cause)
            }
        }
    }

    override fun restoreCredential(recoveryId: String, credentialData: ByteArray): Credential {
        return when (recoveryId) {
            "jwt+credential" -> {
                JWTCredential(credentialData.decodeToString()).toStorableCredential()
            }

            "w3c+credential" -> {
                Json.decodeFromString<W3CCredential>(credentialData.decodeToString())
                    .toStorableCredential()
            }

            else -> {
                throw PolluxError.InvalidCredentialError()
            }
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
        credential: Credential,
        requestPresentationJson: JsonObject
    ): String {
        val parsedPrivateKey = parsePrivateKey(privateKey)
        val domain =
            getDomain(requestPresentationJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        val challenge =
            getChallenge(requestPresentationJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        return signClaimsProofPresentationJWT(
            subjectDID,
            parsedPrivateKey,
            credential,
            domain,
            challenge
        )
    }

    private fun parsePrivateKey(privateKey: PrivateKey): ECPrivateKey {
        val curveName = KMMEllipticCurve.SECP256k1.value
        val sp = ECNamedCurveTable.getParameterSpec(curveName)
        val params: ECParameterSpec = ECNamedCurveSpec(sp.name, sp.curve, sp.g, sp.n, sp.h)
        val privateKeySpec = ECPrivateKeySpec(BigInteger(1, privateKey.getValue()), params)
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
        val signer = ECDSASigner(
            privateKey as java.security.PrivateKey,
            com.nimbusds.jose.jwk.Curve.SECP256K1
        )
        val provider = BouncyCastleProviderSingleton.getInstance()
        signer.jcaContext.provider = provider
        jwsObject.sign(signer)

        // Serialize the JWS object to a string
        return jwsObject.serialize()
    }

    private fun signClaimsProofPresentationJWT(
        subjectDID: DID,
        privateKey: ECPrivateKey,
        credential: Credential,
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
        val signer = ECDSASigner(
            privateKey as java.security.PrivateKey,
            com.nimbusds.jose.jwk.Curve.SECP256K1
        )
        val provider = BouncyCastleProviderSingleton.getInstance()
        signer.jcaContext.provider = provider
        jwsObject.sign(signer)

        // Serialize the JWS object to a string
        return jwsObject.serialize()
    }

    override fun credentialToStorableCredential(credential: Credential): StorableCredential {
        return when (credential::class) {
            JWTCredential::class -> {
                (credential as JWTCredential).toStorableCredential()
            }

            W3CCredential::class -> {
                val w3c: W3CCredential = credential as W3CCredential
                w3c.toStorableCredential()
            }

            else -> {
                throw PolluxError.InvalidCredentialError()
            }
        }
    }
}
