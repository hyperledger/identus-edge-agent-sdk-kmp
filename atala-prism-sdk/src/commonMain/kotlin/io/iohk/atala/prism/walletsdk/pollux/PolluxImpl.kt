package io.iohk.atala.prism.walletsdk.pollux

import anoncreds_wrapper.CredentialDefinition
import anoncreds_wrapper.CredentialOffer
import anoncreds_wrapper.CredentialRequest
import anoncreds_wrapper.CredentialRequestMetadata
import anoncreds_wrapper.LinkSecret
import anoncreds_wrapper.Prover
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.iohk.atala.prism.apollo.utils.KMMEllipticCurve
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.Api
import io.iohk.atala.prism.walletsdk.domain.models.ApiImpl
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.PolluxError
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.domain.models.httpClient
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.pollux.models.AnonCredential
import io.iohk.atala.prism.walletsdk.pollux.models.JWTCredential
import io.iohk.atala.prism.walletsdk.pollux.models.W3CCredential
import io.iohk.atala.prism.walletsdk.prismagent.shared.KeyValue
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.didcommx.didcomm.common.Typ
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.ECParameterSpec
import java.security.spec.ECPrivateKeySpec

class PolluxImpl(
    val castor: Castor,
    private val api: Api = ApiImpl(httpClient())
) : Pollux {

    @Throws(PolluxError.InvalidJWTString::class, PolluxError.InvalidCredentialError::class)
    fun parseVerifiableCredential(data: String): Credential {
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

    override suspend fun parseCredential(
        jsonData: String,
        type: CredentialType,
        linkSecret: LinkSecret?,
        credentialMetadata: CredentialRequestMetadata?
    ): Credential {
        return when (type) {
            CredentialType.JWT -> {
                JWTCredential(jsonData)
            }

            CredentialType.ANONCREDS_ISSUE -> {
                if (linkSecret == null) {
                    throw Error("LinkSecret is required")
                }
                if (credentialMetadata == null) {
                    throw Error("Invalid credential metadata")
                }

                val cred = anoncreds_wrapper.Credential(jsonData)

                val values: Map<String, AnonCredential.Attribute> =
                    cred.getValues().values.mapValues {
                        AnonCredential.Attribute(raw = it.value.raw, encoded = it.value.encoded)
                    }

                return AnonCredential(
                    schemaID = cred.getSchemaId(),
                    credentialDefinitionID = cred.getCredDefId(),
                    signatureJson = cred.getSignatureJson(),
                    signatureCorrectnessProofJson = cred.getSignatureCorrectnessProofJson(),
                    revocationRegistryId = cred.getRevRegId(),
                    revocationRegistryJson = cred.getRevRegJson(),
                    witnessJson = cred.getWitnessJson() ?: "",
                    json = cred.getJson(),
                    values = values
                )
            }

            else -> {
                throw PolluxError.InvalidCredentialError()
            }
        }
    }

    override fun restoreCredential(
        restorationIdentifier: String,
        credentialData: ByteArray
    ): Credential {
        return when (restorationIdentifier) {
            "jwt+credential" -> {
                JWTCredential(credentialData.decodeToString())
            }

            "anon+credential" -> {
                AnonCredential.fromStorableData(credentialData)
            }

            "w3c+credential" -> {
                Json.decodeFromString<W3CCredential>(credentialData.decodeToString())
            }

            else -> {
                throw PolluxError.InvalidCredentialError()
            }
        }
    }

    @Throws(PolluxError.NoDomainOrChallengeFound::class)
    override fun processCredentialRequestJWT(
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

    override fun credentialToStorableCredential(
        type: CredentialType,
        credential: Credential
    ): StorableCredential {
        return when (type) {
            CredentialType.JWT -> {
                (credential as JWTCredential).toStorableCredential()
            }

            CredentialType.W3C -> {
                (credential as W3CCredential).toStorableCredential()
            }

            CredentialType.ANONCREDS_ISSUE -> {
                (credential as AnonCredential).toStorableCredential()
            }

            else -> {
                throw PolluxError.InvalidCredentialError()
            }
        }
    }

    override fun extractCredentialFormatFromMessage(formats: Array<AttachmentDescriptor>): CredentialType {
        val desiredFormats = setOf(
            CredentialType.JWT.type,
            CredentialType.ANONCREDS_OFFER.type,
            CredentialType.ANONCREDS_REQUEST.type,
            CredentialType.ANONCREDS_ISSUE.type
        )
        val foundFormat = formats.find { it.format in desiredFormats }
        return foundFormat?.format?.let { format ->
            when (format) {
                CredentialType.JWT.type -> CredentialType.JWT
                CredentialType.ANONCREDS_OFFER.type -> CredentialType.ANONCREDS_OFFER
                CredentialType.ANONCREDS_REQUEST.type -> CredentialType.ANONCREDS_REQUEST
                CredentialType.ANONCREDS_ISSUE.type -> CredentialType.ANONCREDS_ISSUE
                else -> throw Error("$format is not a valid credential type")
            }
        } ?: throw Error("Unknown credential type")
    }

    override suspend fun processCredentialRequestAnoncreds(
        did: DID,
        offer: CredentialOffer,
        linkSecret: LinkSecret,
        linkSecretName: String
    ): Pair<CredentialRequest, CredentialRequestMetadata> {
        val credentialDefinition = getCredentialDefinition(offer.getCredDefId())

        return createAnonCredentialRequest(
            did = did,
            credentialDefinition = credentialDefinition,
            credentialOffer = offer,
            linkSecret = linkSecret,
            linkSecretId = linkSecretName
        )
    }

    private fun createAnonCredentialRequest(
        did: DID,
        credentialDefinition: CredentialDefinition,
        credentialOffer: CredentialOffer,
        linkSecret: LinkSecret,
        linkSecretId: String
    ): Pair<CredentialRequest, CredentialRequestMetadata> {
        val credentialRequest = Prover().createCredentialRequest(
            entropy = did.toString(),
            proverDid = null,
            credDef = credentialDefinition,
            linkSecret = linkSecret,
            linkSecretId = linkSecretId,
            credentialOffer = credentialOffer
        )
        return Pair(credentialRequest.request, credentialRequest.metadata)
    }

    override suspend fun getCredentialDefinition(id: String): CredentialDefinition {
        val result = api.request(
            HttpMethod.Get.value,
            id,
            emptyArray(),
            arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
            null
        )
        if (result.status == 200) {
            return CredentialDefinition(result.jsonString)
        }
        throw PolluxError.InvalidCredentialDefinitionError()
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
}
