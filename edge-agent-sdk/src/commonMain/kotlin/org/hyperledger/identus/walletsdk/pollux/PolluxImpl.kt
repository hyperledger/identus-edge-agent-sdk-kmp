@file:Suppress("ktlint:standard:import-ordering", "ktlint:standard:wrapping")

package org.hyperledger.identus.walletsdk.pollux

import anoncreds_uniffi.CredentialDefinition
import anoncreds_uniffi.CredentialOffer
import anoncreds_uniffi.CredentialRequest
import anoncreds_uniffi.CredentialRequestMetadata
import anoncreds_uniffi.Presentation
import anoncreds_uniffi.PresentationRequest
import anoncreds_uniffi.Prover
import anoncreds_uniffi.Schema
import anoncreds_uniffi.Verifier
import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.rdf.Rdf
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.setl.rdf.normalization.RdfNormalize
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.didcommx.didcomm.common.Typ
import org.hyperledger.identus.apollo.base64.base64UrlDecoded
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.apollo.utils.KMMECSecp256k1PublicKey
import org.hyperledger.identus.walletsdk.apollo.helpers.gunzip
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.castor.did.prismdid.PrismDIDPublicKey
import org.hyperledger.identus.walletsdk.castor.did.prismdid.defaultId
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pollux
import org.hyperledger.identus.walletsdk.domain.models.AnoncredsInputFieldFilter
import org.hyperledger.identus.walletsdk.domain.models.AnoncredsPresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.ApiImpl
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDDocumentCoreProperty
import org.hyperledger.identus.walletsdk.domain.models.InputFieldFilter
import org.hyperledger.identus.walletsdk.domain.models.JWTPresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.JWTVerifiableCredential
import org.hyperledger.identus.walletsdk.domain.models.KeyValue
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.PresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.RequestedPredicates
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.domain.models.UnknownError
import org.hyperledger.identus.walletsdk.domain.models.httpClient
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurvePointXKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurvePointYKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.ExportableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SignableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.TypeKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.VerifiableKey
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.AnoncredsPresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.JWTPresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptionsAnoncreds
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptionsJWT
import org.hyperledger.identus.walletsdk.logger.Logger
import org.hyperledger.identus.walletsdk.logger.LoggerImpl
import org.hyperledger.identus.walletsdk.logger.LogComponent
import org.hyperledger.identus.walletsdk.pluto.RestorationID
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.AnoncredsPresentationDefinitionRequest
import org.hyperledger.identus.walletsdk.pollux.models.DescriptorItemFormat
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.hyperledger.identus.walletsdk.pollux.models.JWTPresentationDefinitionRequest
import org.hyperledger.identus.walletsdk.pollux.models.JWTProofType
import org.hyperledger.identus.walletsdk.pollux.models.PresentationSubmission
import org.hyperledger.identus.walletsdk.pollux.models.SDJWTCredential
import org.hyperledger.identus.walletsdk.pollux.models.VerificationKeyType
import org.hyperledger.identus.walletsdk.pollux.models.W3CCredential
import org.hyperledger.identus.walletsdk.pollux.utils.BitString

/**
 * Class representing the implementation of the Pollux interface.
 *
 * @property castor An API object for interacting with the Castor system.
 */
@Suppress("LABEL_NAME_CLASH")
open class PolluxImpl(
    val apollo: Apollo,
    val castor: Castor,
    private val api: Api = ApiImpl(httpClient()),
    private val logger: Logger = LoggerImpl(LogComponent.POLLUX)
) : Pollux {

    /**
     * Parses a verifiable credential from the given data.
     *
     * @param data The data representing the*/
    @Throws(PolluxError.InvalidCredentialError::class)
    fun parseVerifiableCredential(data: String): Credential {
        return try {
            JWTCredential.fromJwtString(data)
        } catch (e: Exception) {
            try {
                Json.decodeFromString<W3CCredential>(data)
            } catch (e: Exception) {
                throw PolluxError.InvalidCredentialError(cause = e.cause)
            }
        }
    }

    /**
     * Parses the given JSON data into a verifiable credential of the specified type.
     *
     * @param jsonData The JSON data representing the verifiable credential.
     * @param type The type of the verifiable credential.
     * @param linkSecret The optional link secret for the credential.
     * @param credentialMetadata The metadata for the credential request.
     * @return The parsed credential.
     */
    override suspend fun parseCredential(
        jsonData: String,
        type: CredentialType,
        linkSecret: String?,
        credentialMetadata: CredentialRequestMetadata?
    ): Credential {
        return when (type) {
            CredentialType.JWT -> {
                JWTCredential.fromJwtString(jsonData)
            }

            CredentialType.SDJWT -> {
                SDJWTCredential.fromSDJwtString(jsonData)
            }

            CredentialType.ANONCREDS_ISSUE -> {
                if (linkSecret == null) {
                    throw Error("LinkSecret is required")
                }
                if (credentialMetadata == null) {
                    throw Error("Invalid credential metadata")
                }

                val cred = anoncreds_uniffi.Credential(jsonData)
                val credentialDefinition = getCredentialDefinition(cred.credDefId())
                val prover = Prover()
                val processedCredential = prover.processCredential(
                    cred = cred,
                    credReqMetadata = credentialMetadata,
                    linkSecret = linkSecret,
                    credDef = credentialDefinition,
                    revRegDef = null
                )

                val values: Map<String, AnonCredential.Attribute> =
                    processedCredential.values().mapValues {
                        AnonCredential.Attribute(raw = it.key, encoded = it.value)
                    }

                val jsonProcessedCredential = Json.parseToJsonElement(processedCredential.toJson())
                return AnonCredential(
                    schemaID = processedCredential.schemaId(),
                    credentialDefinitionID = processedCredential.credDefId(),
                    signatureJson = jsonProcessedCredential.jsonObject["signature"]?.toString() ?: "",
                    signatureCorrectnessProofJson = jsonProcessedCredential.jsonObject["signature_correctness_proof"]?.toString()
                        ?: "",
                    revocationRegistryId = processedCredential.revRegId(),
                    revocationRegistryJson = jsonProcessedCredential.jsonObject["rev_reg"]?.toString(),
                    witnessJson = jsonProcessedCredential.jsonObject["witness"]?.toString(),
                    json = processedCredential.toJson(),
                    values = values
                )
            }

            else -> {
                throw PolluxError.InvalidCredentialError()
            }
        }
    }

    /**
     * Restores a credential using the provided restoration identifier and credential data.
     *
     * @param restorationIdentifier The restoration identifier of the credential.
     * @param credentialData The byte array containing the credential data.
     * @return The restored credential.
     */
    override fun restoreCredential(
        restorationIdentifier: String,
        credentialData: ByteArray,
        revoked: Boolean
    ): Credential {
        val cred: Credential
        when (restorationIdentifier) {
            "sd-jwt+credential" -> {
                cred = SDJWTCredential.fromSDJwtString(credentialData.decodeToString())
            }

            RestorationID.JWT.value -> {
                cred = JWTCredential.fromJwtString(credentialData.decodeToString())
            }

            RestorationID.ANONCRED.value -> {
                cred = AnonCredential.fromStorableData(credentialData)
            }

            RestorationID.W3C.value -> {
                cred = Json.decodeFromString<W3CCredential>(credentialData.decodeToString())
            }

            else -> {
                throw PolluxError.InvalidCredentialError()
            }
        }
        cred.revoked = revoked
        return cred
    }

    /**
     * Creates a verifiable presentation JSON Web Token (JWT) for the given subjectDID, privateKey, credential, and requestPresentationJson.
     *
     * @param subjectDID The DID of the subject for whom the presentation is being created.
     * @param privateKey The private key used to sign the JWT.
     * @param credential The credential to be included in the presentation.
     * @param requestPresentationJson The JSON object representing the request presentation.
     * @return The created verifiable presentation JWT.
     */
    @Throws(PolluxError.NoDomainOrChallengeFound::class)
    override suspend fun processCredentialRequestJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        offerJson: JsonObject
    ): String {
        val domain = getDomain(offerJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        val challenge = getChallenge(offerJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        return signClaimsRequestCredentialJWT(subjectDID, privateKey, domain, challenge)
    }

    /**
     * Creates a verifiable presentation JSON Web Token (JWT) for the given subjectDID, privateKey, credential, and requestPresentationJson.
     *
     * @param subjectDID The DID of the subject for whom the presentation is being created.
     * @param privateKey The private key used to sign the JWT.
     * @param credential The credential to be included in the presentation.
     * @param requestPresentationJson The JSON object representing the request presentation.
     * @return The created verifiable presentation JWT.
     */
    @Throws(PolluxError.NoDomainOrChallengeFound::class)
    override suspend fun processCredentialRequestSDJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        offerJson: JsonObject
    ): String {
        val domain = getDomain(offerJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        val challenge = getChallenge(offerJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        return signClaimsRequestCredentialJWT(subjectDID, privateKey, domain, challenge)
    }

    /**
     * Converts a [Credential] object to a [StorableCredential] object of the specified [CredentialType].
     *
     * @param type The type of the [StorableCredential].
     * @param credential The [Credential] object to be converted.
     * @return The converted [StorableCredential].
     */
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

    /**
     * Extracts the credential format from the given array of attachment descriptors.
     *
     * @param formats The array of attachment descriptors.
     * @return The credential format as a CredentialType enum value.
     */
    override fun extractCredentialFormatFromMessage(formats: Array<AttachmentDescriptor>): CredentialType {
        val desiredFormats = setOf(
            CredentialType.JWT.type,
            CredentialType.ANONCREDS_OFFER.type,
            CredentialType.ANONCREDS_REQUEST.type,
            CredentialType.ANONCREDS_ISSUE.type,
            CredentialType.ANONCREDS_PROOF_REQUEST.type
        )
        val foundFormat = formats.find { it.format in desiredFormats }
        return foundFormat?.format?.let { format ->
            when (format) {
                CredentialType.JWT.type -> CredentialType.JWT
                CredentialType.ANONCREDS_OFFER.type -> CredentialType.ANONCREDS_OFFER
                CredentialType.ANONCREDS_REQUEST.type -> CredentialType.ANONCREDS_REQUEST
                CredentialType.ANONCREDS_ISSUE.type -> CredentialType.ANONCREDS_ISSUE
                CredentialType.ANONCREDS_PROOF_REQUEST.type -> CredentialType.ANONCREDS_PROOF_REQUEST
                else -> throw Error("$format is not a valid credential type")
            }
        } ?: throw Error("Unknown credential type")
    }

    /**
     * Processes a credential request for anonymous credentials.
     *
     * @param did The DID of the subject requesting the credential.
     * @param offer The credential offer.
     * @param linkSecret The link secret for the credential.
     * @param linkSecretName The name of the link secret.
     * @return A pair containing the credential request and its metadata.
     */
    override suspend fun processCredentialRequestAnoncreds(
        did: DID,
        offer: CredentialOffer,
        linkSecret: String,
        linkSecretName: String
    ): Pair<CredentialRequest, CredentialRequestMetadata> {
        val credentialDefinition = getCredentialDefinition(offer.credDefId())

        return createAnonCredentialRequest(
            did = did,
            credentialDefinition = credentialDefinition,
            credentialOffer = offer,
            linkSecret = linkSecret,
            linkSecretId = linkSecretName
        )
    }

    /**
     * Creates a credential request for anonymous credentials.
     *
     * @param did The DID of the subject requesting the credential.
     * @param credentialDefinition The credential definition.
     * @param credentialOffer The credential offer.
     * @param linkSecret The link secret for the credential.
     * @param linkSecretId The name of the link secret.
     * @return A Pair containing the CredentialRequest and CredentialRequestMetadata.
     */
    private fun createAnonCredentialRequest(
        did: DID,
        credentialDefinition: CredentialDefinition,
        credentialOffer: CredentialOffer,
        linkSecret: String,
        linkSecretId: String
    ): Pair<CredentialRequest, CredentialRequestMetadata> {
        val credentialRequest = Prover().createCredentialRequest(
            entropy = did.toString(),
            proverDid = null,
            credDef = credentialDefinition,
            linkSecret = linkSecret,
            linkSecretId = linkSecretId,
            credOffer = credentialOffer
        )
        return Pair(credentialRequest.request, credentialRequest.metadata)
    }

    /**
     * Retrieves the credential definition for the specified ID.
     *
     * @param id The ID of the credential definition.
     * @return The credential definition.
     */
    suspend fun getCredentialDefinition(id: String): CredentialDefinition {
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

    override suspend fun getSchema(schemaId: String): Schema {
        val result = api.request(
            HttpMethod.Get.value,
            schemaId,
            emptyArray(),
            arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
            null
        )

        if (result.status == 200) {
            return Schema(result.jsonString)
        }
        throw PolluxError.InvalidCredentialDefinitionError()
    }

    override suspend fun isCredentialRevoked(credential: Credential): Boolean {
        if (credential !is JWTCredential) {
            throw PolluxError.InvalidCredentialError()
        }
        if (credential.verifiableCredential?.credentialStatus == null) {
            throw PolluxError.InvalidJWTCredential("Credential must contain credential status")
        }
        credential.verifiableCredential!!.credentialStatus?.let { credentialStatus ->
            val revocationRegistryJson = fetchRevocationRegistry(credentialStatus)
            validateProof(revocationRegistryJson)
            return checkEncodedListRevoked(revocationRegistryJson, credentialStatus.statusListIndex)
        } ?: return false
    }

    private suspend fun validateProof(revocationRegistryJson: String): Boolean {
        val jsonObject = (Json.parseToJsonElement(revocationRegistryJson) as JsonObject)
        if (!jsonObject.containsKey("proof")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("proof")
        }
        val proof = jsonObject["proof"]!!.jsonObject
        if (!proof.containsKey("verificationMethod")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("Proof->verificationMethod")
        }
        if (!proof.containsKey("type")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("Proof->type")
        }
        val verificationMethod = proof["verificationMethod"]
        val proofType = proof["type"]?.jsonPrimitive?.content
        val base64VerificationMethod = verificationMethod!!.jsonPrimitive.content.split(",")[1]
        val decodedVerificationMethod =
            Json.parseToJsonElement(base64VerificationMethod.base64UrlDecoded) as JsonObject
        if (proofType != JWTProofType.ECDSASECP256K1Signature2019.value) {
            throw PolluxError.UnsupportedTypeError(proofType.toString())
        }
        if (!decodedVerificationMethod.containsKey("type")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("DecodedVerificationMethod->type")
        }
        if (!decodedVerificationMethod.containsKey("publicKeyJwk")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("DecodedVerificationMethod->publicKeyJwk")
        }
        val verificationMethodType = decodedVerificationMethod["type"]?.jsonPrimitive?.content
        val publicKeyJwk = decodedVerificationMethod["publicKeyJwk"]?.jsonObject

        if (verificationMethodType != VerificationKeyType.EcdsaSecp256k1VerificationKey2019.value) {
            throw PolluxError.UnsupportedTypeError(verificationMethodType.toString())
        }
        if (!publicKeyJwk!!.containsKey("x")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("x")
        }
        if (!publicKeyJwk.containsKey("y")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("y")
        }
        if (!publicKeyJwk.containsKey("kty")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("kty")
        }
        if (!publicKeyJwk.containsKey("crv")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("crv")
        }

        val x = publicKeyJwk["x"]?.jsonPrimitive?.content
        val y = publicKeyJwk["y"]?.jsonPrimitive?.content
        val kty = publicKeyJwk["kty"]?.jsonPrimitive?.content
        val crv = publicKeyJwk["crv"]?.jsonPrimitive?.content
        if (x == null || y == null || kty == null || crv == null) {
            throw PolluxError.NonNullableError("$x, $y, $kty, $crv")
        }

        if (kty != KeyTypes.EC.type || crv != Curve.SECP256K1.value.lowercase()) {
            throw PolluxError.UnsupportedTypeError(kty)
        }

        val publicKey = apollo.createPublicKey(
            mapOf(
                TypeKey().property to KeyTypes.EC,
                CurveKey().property to crv,
                CurvePointXKey().property to x,
                CurvePointYKey().property to y
            )
        )
        if (!publicKey.canVerify()) {
            throw PolluxError.VerifyProofError()
        }

        if (!proof.containsKey("jws")) {
            throw PolluxError.RevocationRegistryJsonMissingFieldError("Proof->jws")
        }
        val jwsArray = proof["jws"]?.jsonPrimitive?.content?.split(".") ?: throw PolluxError.InvalidJWTString()
        if (jwsArray.size != 3) {
            throw PolluxError.InvalidJWTString()
        }

        val payload = JsonObject(jsonObject.filterKeys { it != "proof" })
        val encoded = encode(Json.encodeToString(payload))
        val signaturePayload = "${jwsArray[0]}.$encoded"
        val signature = jwsArray[2].base64UrlDecodedBytes

        val verified = (publicKey as VerifiableKey).verify(
            signaturePayload.toByteArray(),
            signature
        )
        if (!verified) {
            throw PolluxError.VerifyProofError()
        }
        return true
    }

    private fun encode(data: String): String {
        val inputStream = ByteArrayInputStream(data.toByteArray())
        val document = JsonDocument.of(inputStream)
        val rdfDataset = JsonLd.toRdf(document).get()
        val normalized = RdfNormalize.normalize(rdfDataset)
        val writer = StringWriter()
        val rdfWriter = Rdf.createWriter(com.apicatalog.jsonld.http.media.MediaType.N_QUADS, writer)
        rdfWriter.write(normalized)

        return writer.toString()
    }

    fun checkEncodedListRevoked(revocationRegistryJson: String, statusListIndex: Int): Boolean {
        val jsonObject = (Json.parseToJsonElement(revocationRegistryJson) as JsonObject)
        if (jsonObject.containsKey("credentialSubject")) {
            val credentialSubject = jsonObject["credentialSubject"]!!.jsonObject
            if (credentialSubject.containsKey("encodedList")) {
                val encodedList = credentialSubject["encodedList"]?.jsonPrimitive?.content
                if (encodedList != null) {
                    return verifyStatusListIndexForEncodedList(encodedList, statusListIndex)
                }
            }
        }
        return false
    }

    fun verifyStatusListIndexForEncodedList(encodedList: String, statusListIndex: Int): Boolean {
        val decodedBytes = Base64.getUrlDecoder().decode(encodedList)
        val decompressedBytes =
            decodedBytes.gunzip()
        val bitString = BitString(decompressedBytes)

        if (statusListIndex > decompressedBytes.size) {
            throw PolluxError.StatusListOutOfBoundIndex()
        }
        return bitString.isRevoked(statusListIndex)
    }

    suspend fun fetchRevocationRegistry(credentialStatus: JWTVerifiableCredential.CredentialStatus): String {
        val result = api.request(
            HttpMethod.Get.value,
            credentialStatus.statusListCredential,
            emptyArray(),
            arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
            null
        )
        if (result.status == 200) {
            return result.jsonString
        }
        throw UnknownError.SomethingWentWrongError("Fetch revocation registry failed: ${result.jsonString}")
    }

    /**
     * Parses a PrivateKey into an ECPrivateKey.
     *
     * @param privateKey The PrivateKey to parse.
     * @return The parsed ECPrivateKey.
     */
    internal fun parsePrivateKey(privateKey: PrivateKey): ECPrivateKey {
        return privateKey.jca() as ECPrivateKey
    }

    private fun parsePublicKey(publicKey: PublicKey): ECPublicKey {
        return publicKey.jca() as ECPublicKey
    }

    /**
     * Returns the domain from the given JsonObject.
     *
     * @param jsonObject The JsonObject from which to retrieve the domain.
     * @return The domain as a String, or null if not found.
     */
    private fun getDomain(jsonObject: JsonObject): String? {
        return jsonObject[OPTIONS]?.jsonObject?.get(DOMAIN)?.jsonPrimitive?.content
    }

    /**
     * Retrieves the challenge value from the given JsonObject.
     *
     * @param jsonObject The JsonObject from which to retrieve the challenge.
     * @return The challenge value as a String, or null if not found in the JsonObject.
     */
    private fun getChallenge(jsonObject: JsonObject): String? {
        return jsonObject[OPTIONS]?.jsonObject?.get(CHALLENGE)?.jsonPrimitive?.content
    }

    /**
     * Signs the claims for a request credential JWT.
     *
     * @param subjectDID The DID of the subject for whom the JWT is being created.
     * @param privateKey The private key used to sign the JWT.
     * @param domain The domain of the JWT.
     * @param challenge The challenge value for the JWT.
     * @return The signed JWT as a string.
     */
    private suspend fun signClaimsRequestCredentialJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        domain: String,
        challenge: String
    ): String {
        return signClaims(subjectDID, privateKey, domain, challenge)
    }

    /**
     * Signs the claims for a proof presentation JSON Web Token (JWT).
     *
     * @param subjectDID The DID of the subject for whom the JWT is being created.
     * @param privateKey The private key used to sign the JWT.
     * @param credential The credential to be included in the presentation.
     * @param domain The domain of the JWT.
     * @param challenge The challenge value for the JWT.
     * @return The signed JWT as a string.
     */
    private suspend fun signClaimsProofPresentationJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        credential: Credential,
        domain: String,
        challenge: String
    ): String {
        return signClaims(subjectDID, privateKey, domain, challenge, credential)
    }

    /**
     * Signs the claims for a proof presentation JSON Web Token (JWT).
     *
     * @param subjectDID The DID of the subject for whom the JWT is being created.
     * @param privateKey The private key used to sign the JWT.
     * @param credential The credential to be included in the presentation.
     * @param domain The domain of the JWT.
     * @param challenge The challenge value for the JWT.
     * @return The signed JWT as a string.
     */
    internal suspend fun signClaimsProofPresentationSDJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        credential: Credential,
        domain: String,
        challenge: String
    ): String {
        return signClaims(subjectDID, privateKey, domain, challenge, credential)
    }

    /**
     * Signs the claims for a JWT.
     *
     * @param subjectDID The DID of the subject for whom the JWT is being created.
     * @param privateKey The private key used to sign the JWT.
     * @param domain The domain of the JWT.
     * @param challenge The challenge value for the JWT.
     * @param credential The optional credential to be included in the JWT.
     * @return The signed JWT as a string.
     */
    internal suspend fun signClaims(
        subjectDID: DID,
        privateKey: PrivateKey,
        domain: String,
        challenge: String,
        credential: Credential? = null
    ): String {
        if (privateKey !is ExportableKey) {
            throw PolluxError.PrivateKeyTypeNotSupportedError("The private key should be ${ExportableKey::class.simpleName}")
        }
        val ecPrivateKey = parsePrivateKey(privateKey)

        val presentation: MutableMap<String, Collection<String>> = mutableMapOf(
            CONTEXT to setOf(CONTEXT_URL),
            TYPE to setOf(VERIFIABLE_PRESENTATION)
        )
        credential?.let {
            presentation[VERIFIABLE_CREDENTIAL] = listOf(it.id)
        }

        val claims = JWTClaimsSet.Builder()
            .issuer(subjectDID.toString())
            .audience(domain)
            .claim(NONCE, challenge)
            .claim(VP, presentation)
            .build()

        val kid = getSigningKid(subjectDID)

        // Generate a JWS header with the ES256K algorithm
        val header = JWSHeader.Builder(JWSAlgorithm.ES256K)
            .keyID(kid)
            .build()

        // Sign the JWT with the private key
        val jwsObject = SignedJWT(header, claims)
        val signer = ECDSASigner(
            ecPrivateKey as java.security.PrivateKey,
            com.nimbusds.jose.jwk.Curve.SECP256K1
        )
        val provider = BouncyCastleProviderSingleton.getInstance()
        signer.jcaContext.provider = provider
        jwsObject.sign(signer)

        // Serialize the JWS object to a string
        return jwsObject.serialize()
    }

    override suspend fun createPresentationDefinitionRequest(
        type: CredentialType,
        presentationClaims: PresentationClaims,
        options: PresentationOptions
    ): String {
        val format: JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor.PresentationFormat
        val inputDescriptor: JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor
        when (type) {
            CredentialType.JWT -> {
                if (options !is JWTPresentationOptions) {
                    throw PolluxError.PresentationDefinitionRequestError("When type is ${type.type}, presentation options must be ${JWTPresentationOptions::class.simpleName}")
                }
                if (presentationClaims !is JWTPresentationClaims) {
                    throw PolluxError.PresentationDefinitionRequestError("When type is ${type.type}, presentation claims must be ${JWTPresentationClaims::class.simpleName}")
                }
                val jwt = options.jwt
                if (jwt.isEmpty()) {
                    throw PolluxError.InvalidJWTPresentationDefinitionError("Presentation option must contain at least one valid JWT alg that is not empty.")
                }
                val paths = presentationClaims.claims.keys
                val mutableListFields: MutableList<JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.Field> =
                    paths.map { path ->
                        JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.Field(
                            path = arrayOf("$.vc.credentialSubject.$path", "$.credentialSubject.$path"),
                            id = UUID.randomUUID().toString(),
                            optional = false,
                            filter = presentationClaims.claims[path],
                            name = path,
                        )
                    } as MutableList

                presentationClaims.issuer?.let { issuer ->
                    mutableListFields.add(
                        JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.Field(
                            path = arrayOf("$.issuer", "$.iss", "$.vc.iss", "$.vc.issuer"),
                            optional = false,
                            id = UUID.randomUUID().toString(),
                            filter = InputFieldFilter(
                                type = "String",
                                pattern = issuer
                            ),
                            name = "issuer"
                        )
                    )
                }

                val constraints = JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints(
                    fields = mutableListFields.toTypedArray(),
                    limitDisclosure =
                    JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.LimitDisclosure.REQUIRED
                )

                format =
                    JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor.PresentationFormat(
                        jwt = jwt.let {
                            JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor.JwtFormat(
                                jwt.toList()
                            )
                        }
                    )

                inputDescriptor = JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor(
                    name = options.name,
                    purpose = options.purpose,
                    constraints = constraints,
                    format = format
                )

                return Json.encodeToString(
                    JWTPresentationDefinitionRequest(
                        presentationDefinition = JWTPresentationDefinitionRequest.PresentationDefinition(
                            inputDescriptors = arrayOf(inputDescriptor),
                            format = format
                        ),
                        options = JWTPresentationDefinitionRequest.PresentationDefinitionOptions(
                            domain = options.domain,
                            challenge = options.challenge
                        )
                    )
                )
            }

            CredentialType.ANONCREDS_PROOF_REQUEST -> {
                if (options !is AnoncredsPresentationOptions) {
                    throw PolluxError.PresentationDefinitionRequestError("When type is ${type.type}, presentation options must be ${AnoncredsPresentationOptions::class.simpleName}")
                }
                if (presentationClaims !is AnoncredsPresentationClaims) {
                    throw PolluxError.PresentationDefinitionRequestError("When type is ${type.type}, presentation options must be ${AnoncredsPresentationClaims::class.simpleName}")
                }
                val predicates = presentationClaims.predicates

                val mapPredicate = mutableMapOf<String, RequestedPredicates>()
                predicates.forEach { predicate ->
                    val key = predicate.key
                    val inputFilter: AnoncredsInputFieldFilter = predicate.value
                    val pType =
                        if (inputFilter.gt != null) {
                            ">"
                        } else if (inputFilter.gte != null) {
                            ">="
                        } else if (inputFilter.lt != null) {
                            "<"
                        } else if (inputFilter.lte != null) {
                            "<="
                        } else {
                            throw Exception() // TODO: Custom exception
                        }

                    val pValue = (inputFilter.gt
                        ?: (inputFilter.gte
                            ?: (inputFilter.lt
                                ?: (inputFilter.lte ?: throw Exception())))) as Int // TODO: Custom excetion

                    // Based on the definition of AnoncredsPresentationClaims we do not need to verify if key is duplicated.
                    mapPredicate[key] = RequestedPredicates(
                        name = inputFilter.name,
                        pType = pType,
                        pValue = pValue,
                        restrictions = emptyMap(),
                        nonRevoked = null
                    )
                }

                return Json.encodeToString(
                    AnoncredsPresentationDefinitionRequest(
                        nonce = options.nonce,
                        name = "anoncreds_presentation_request",
                        version = "1.0",
                        requestedPredicates = mapPredicate,
                        requestedAttributes = presentationClaims.attributes
                    )
                )
            }

            else -> {
                throw PolluxError.CredentialTypeNotSupportedError(
                    "Credential type ${type.type} not supported. " +
                        "Must be ${CredentialType.JWT.type} or ${CredentialType.ANONCREDS_PROOF_REQUEST.type}"
                )
            }
        }
    }

    override suspend fun createJWTPresentationSubmission(
        presentationDefinitionRequest: String,
        credential: Credential,
        privateKey: PrivateKey
    ): String {
        if (credential::class != JWTCredential::class) {
            throw PolluxError.CredentialTypeNotSupportedError("Credential type not supported, must be JWTCredential")
        }
        if (privateKey == null || privateKey::class != Secp256k1PrivateKey::class) {
            throw PolluxError.PrivateKeyTypeNotSupportedError()
        }
        privateKey as Secp256k1PrivateKey
        credential as JWTCredential
        val jwtPresentationDefinitionRequest =
            Json.decodeFromString<JWTPresentationDefinitionRequest>(presentationDefinitionRequest)
        val descriptorItems =
            jwtPresentationDefinitionRequest.presentationDefinition.inputDescriptors.map { inputDescriptor ->
                if (inputDescriptor.format != null && (inputDescriptor.format.jwt == null || inputDescriptor.format.jwt.alg.isEmpty())) {
                    throw PolluxError.InvalidCredentialDefinitionError()
                }
                PresentationSubmission.Submission.DescriptorItem(
                    id = inputDescriptor.id,
                    format = DescriptorItemFormat.JWT_VP.value,
                    path = "$.verifiablePresentation[0]",
                    pathNested = PresentationSubmission.Submission.DescriptorItem(
                        id = inputDescriptor.id,
                        format = DescriptorItemFormat.JWT_VC.value,
                        path = "$.vp.verifiableCredential[0]"
                    )
                )
            }.toTypedArray()

        val credentialSubject = credential.subject
        credentialSubject?.let { subject ->
            if (!privateKey.isSignable()) {
                throw PolluxError.WrongKeyProvided(
                    expected = SignableKey::class.simpleName,
                    actual = privateKey::class.simpleName
                )
            }
            val signedChallenge =
                privateKey.sign(jwtPresentationDefinitionRequest.options.challenge.encodeToByteArray())

            val presentationJwt = signClaimsProofPresentationJWT(
                subjectDID = DID(subject),
                privateKey = privateKey,
                credential = credential,
                domain = jwtPresentationDefinitionRequest.options.domain,
                challenge = jwtPresentationDefinitionRequest.options.challenge
            )

            return Json.encodeToString(
                PresentationSubmission(
                    presentationSubmission = PresentationSubmission.Submission(
                        definitionId = jwtPresentationDefinitionRequest.presentationDefinition.id
                            ?: UUID.randomUUID().toString(),
                        descriptorMap = descriptorItems
                    ),
                    verifiablePresentation = arrayOf(presentationJwt)
                )
            )
        } ?: throw PolluxError.NonNullableError("CredentialSubject")
    }

    override suspend fun createAnoncredsPresentationSubmission(
        presentationDefinitionRequest: String,
        credential: Credential,
        linkSecret: String
    ): String {
        if (credential::class != AnonCredential::class) {
            throw PolluxError.CredentialTypeNotSupportedError("Credential type not supported, must be AnonCredential")
        }
        credential as AnonCredential
        val presentationRequest = PresentationRequest(presentationDefinitionRequest)
        val cred = anoncreds_uniffi.Credential(credential.id)

        val requestedAttributes = extractRequestedAttributes(presentationDefinitionRequest)
        val requestedPredicates = extractRequestedPredicatesKeys(presentationDefinitionRequest)

        val credentialRequests = anoncreds_uniffi.RequestedCredential(
            cred = cred,
            timestamp = null,
            revState = null,
            requestedAttributes = requestedAttributes,
            requestedPredicates = requestedPredicates
        )

        val schemaId = credential.schemaID
        // When testing using a local instance of an agent, we need to replace the host.docker.internal with the local IP
        // .replace("host.docker.internal", "192.168.68.114")
        val schema = getSchema(schemaId)

        val schemaMap: Map<String, Schema> = mapOf(Pair(credential.schemaID, schema))

        val credDefId = credential.credentialDefinitionID
        // When testing using a local instance of an agent, we need to replace the host.docker.internal with the local IP
        // .replace("host.docker.internal", "192.168.68.114")
        val credentialDefinition = getCredentialDefinition(credDefId)
        val credDefinition: Map<String, CredentialDefinition> = mapOf(
            Pair(credential.credentialDefinitionID, credentialDefinition)
        )
        return Prover().createPresentation(
            presReq = presentationRequest,
            requestedCredentials = listOf(credentialRequests),
            selfAttestedAttributes = null,
            linkSecret = linkSecret,
            schemas = schemaMap,
            credDefs = credDefinition
        ).toJson()
    }

    /**
     * Converts the map of [anoncreds_uniffi.AttributeInfoValue] values to a list of [RequestedAttribute].
     *
     * @return The list of [RequestedAttribute].
     */
    private fun extractRequestedAttributes(jsonString: String): Map<String, Boolean> {
        val jsonElement: JsonElement = Json.parseToJsonElement(jsonString)
        val jsonObject: JsonObject = jsonElement.jsonObject
        val requestedAttributes = jsonObject["requested_attributes"]?.jsonObject ?: JsonObject(mapOf())
        val resultMap = requestedAttributes.keys.associateWith { true }

        return resultMap
    }

    /**
     * Converts the map of [anoncreds_uniffi.PredicateInfoValue] values to a list of [RequestedPredicate].
     * @receiver The map of [anoncreds_uniffi.PredicateInfoValue] values.
     */
    private fun extractRequestedPredicatesKeys(jsonString: String): List<String> {
        val jsonElement: JsonElement = Json.parseToJsonElement(jsonString)
        val jsonObject: JsonObject = jsonElement.jsonObject
        val requestedPredicates = jsonObject["requested_predicates"]?.jsonObject ?: JsonObject(mapOf())
        val keysList = requestedPredicates.keys.toList()

        return keysList
    }

    override suspend fun verifyPresentationSubmission(
        presentationSubmissionString: String,
        options: PresentationSubmissionOptions
    ): Boolean {
        if (options::class == PresentationSubmissionOptionsJWT::class) {
            val presentationSubmissionJsonObject =
                Json.decodeFromString<JsonElement>(presentationSubmissionString).jsonObject
            val presentationSubmission =
                presentationSubmissionJsonObject["presentation_submission"]?.let { presentationSubmissionField ->
                    val submission =
                        Json.decodeFromJsonElement<PresentationSubmission.Submission>(
                            presentationSubmissionField
                        )
                    var arrayStrings: Array<String> = arrayOf()

                    if (submission.descriptorMap.isNotEmpty()) {
                        val firstDescriptorItem = submission.descriptorMap.first()
                        // Assume the path denotes a direct key in the JSON and strip out JSONPath or XPath specific characters if any.
                        val path = firstDescriptorItem.path.removePrefix("$.")
                            .removeSuffix("[0]") // Adjust based on actual path format
                        arrayStrings =
                            presentationSubmissionJsonObject[path]?.jsonArray?.map { it.jsonPrimitive.content }
                                ?.toTypedArray()
                                ?: arrayOf()
                    }
                    return@let PresentationSubmission(submission, arrayStrings)
                } ?: throw PolluxError.VerificationUnsuccessful("Presentation is missing presentation_submission")

//            val presentationDefinitionRequest =
//                (options as PresentationSubmissionOptionsJWT).presentationDefinitionRequest as JWTPresentationDefinitionRequest
//            presentationDefinitionRequest.presentationDefinition
//            val inputDescriptors =
//                presentationDefinitionRequest.presentationDefinition.inputDescriptors
            val presentationDefinitionRequestString =
                (options as PresentationSubmissionOptionsJWT).presentationDefinitionRequest
            val presentationDefinitionRequest =
                Json.decodeFromString<JWTPresentationDefinitionRequest>(presentationDefinitionRequestString)
            val inputDescriptors = presentationDefinitionRequest.presentationDefinition.inputDescriptors
            val descriptorMap = DescriptorPath(Json.encodeToJsonElement(presentationSubmission))
            val descriptorMaps = presentationSubmission.presentationSubmission.descriptorMap
            descriptorMaps.forEach { descriptorItem ->
                if (descriptorItem.format != DescriptorItemFormat.JWT_VP.value) {
                    throw PolluxError.VerificationUnsuccessful("Invalid submission, ${descriptorItem.path} expected to have format ${DescriptorItemFormat.JWT_VP.value}")
                }

                var newPath: String? = null
                if (!descriptorItem.path.contains("verifiablePresentation")) {
                    newPath =
                        PresentationSubmission.Submission.DescriptorItem.replacePathWithVerifiablePresentation(
                            descriptorItem.path
                        )
                }
                val jws =
                    descriptorMap.getValue(newPath ?: descriptorItem.path)
                        ?: throw PolluxError.VerificationUnsuccessful("Could not find ${descriptorItem.path} value")
                val presentation = JWTCredential.fromJwtString(jws as String)
                val issuer = presentation.issuer

                val presentationDefinitionOptions = presentationDefinitionRequest.options
                val challenge = presentationDefinitionOptions.challenge
                if (challenge.isNotBlank()) {
                    val nonce = presentation.nonce
                    if (nonce.isNullOrBlank()) {
                        throw PolluxError.VerificationUnsuccessful("Invalid submission, ${descriptorItem.path} does snot contain a nonce with a valid signature for the challenge.")
                    }
                    if (challenge != nonce) {
                        throw PolluxError.VerificationUnsuccessful("Invalid submission, the signature from ${descriptorItem.path} is not valid for the challenge.")
                    }
                }

                descriptorItem.pathNested?.let { pathNested ->
                    val verifiableCredentialMapper =
                        DescriptorPath(Json.encodeToJsonElement(presentation))
                    val value = verifiableCredentialMapper.getValue(pathNested.path)
                    value?.let { vc ->
                        val verifiableCredential = JWTCredential.fromJwtString(vc as String)

                        val isRevoked = isCredentialRevoked(verifiableCredential)

                        if (isRevoked) {
                            throw PolluxError.VerificationUnsuccessful("Provided credential is revoked")
                        }

                        if (verifiableCredential.subject != issuer) {
                            throw PolluxError.VerificationUnsuccessful("Invalid submission,")
                        }

                        val didDocHolder = castor.resolveDID(verifiableCredential.issuer)
                        val authenticationMethodHolder =
                            didDocHolder.coreProperties.find { it::class == DIDDocument.Authentication::class }
                                ?: throw PolluxError.VerificationUnsuccessful("Holder core properties must contain Authentication")
                        val ecPublicKeysHolder =
                            extractEcPublicKeyFromVerificationMethod(authenticationMethodHolder)

                        if (!verifyJWTSignatureWithEcPublicKey(
                                verifiableCredential.id,
                                ecPublicKeysHolder
                            )
                        ) {
                            throw PolluxError.VerificationUnsuccessful("Invalid presentation credential JWT Signature")
                        }

                        // Now we are going to validate the requested fields with the provided credentials
                        val verifiableCredentialDescriptorPath =
                            DescriptorPath(Json.encodeToJsonElement(verifiableCredential))
                        val inputDescriptor =
                            inputDescriptors.find { it.id == descriptorItem.id }
                        if (inputDescriptor != null) {
                            val constraints = inputDescriptor.constraints
                            val fields = constraints.fields
                            if (constraints.limitDisclosure == JWTPresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.LimitDisclosure.REQUIRED) {
                                fields?.forEach { field ->
                                    val optional = field.optional
                                    if (!optional) {
                                        var validClaim = false
                                        var reason = ""
                                        val paths = field.path
                                        paths.forEach { path ->
                                            val fieldValue =
                                                verifiableCredentialDescriptorPath.getValue(path)
                                            if (fieldValue != null) {
                                                if (field.filter != null) {
                                                    val filter: InputFieldFilter = field.filter
                                                    filter.pattern?.let { pattern ->
                                                        val regexPattern = Regex(pattern)
                                                        if (regexPattern.matches(fieldValue.toString()) || fieldValue == pattern) {
                                                            validClaim = true
                                                            return@forEach
                                                        } else {
                                                            reason =
                                                                "Expected the $path field to be $pattern but got $fieldValue"
                                                        }
                                                    }
                                                    filter.enum?.let { enum ->
                                                        enum.forEach { predicate ->
                                                            if (fieldValue == predicate) {
                                                                validClaim = true
                                                                return@forEach
                                                            }
                                                        }
                                                        if (!validClaim) {
                                                            reason =
                                                                "Expected the $path field to be one of ${filter.enum.joinToString { ", " }} but got $fieldValue"
                                                        }
                                                    }
                                                    filter.const?.let { const ->
                                                        const.forEach { constValue ->
                                                            if (fieldValue == constValue) {
                                                                validClaim = true
                                                                return@forEach
                                                            }
                                                        }
                                                        if (!validClaim) {
                                                            reason =
                                                                "Expected the $path field to be one of ${filter.const.joinToString { ", " }} but got $fieldValue"
                                                        }
                                                    }
                                                    filter.value?.let { value ->
                                                        if (value == fieldValue) {
                                                            validClaim = true
                                                            return@forEach
                                                        } else {
                                                            reason =
                                                                "Expected the $path field to be $value but got $fieldValue"
                                                        }
                                                    }
                                                } else {
                                                    reason =
                                                        "Input field filter for ${field.name} is null"
                                                }
                                            } else {
                                                reason = "Field value for path $path is null"
                                            }
                                        }
                                        if (!validClaim) {
                                            throw PolluxError.VerificationUnsuccessful(reason)
                                        }
                                    }
                                }
                            }
                        }
                    }
                        ?: throw PolluxError.VerificationUnsuccessful("Invalid submission, no value found for $pathNested")
                    return true
                }
            }
        } else if (options::class == PresentationSubmissionOptionsAnoncreds::class) {
            val presentationDefinitionRequest =
                (options as PresentationSubmissionOptionsAnoncreds).presentationDefinitionRequest
            val anoncredsPresentationDefinitionRequest =
                Json.decodeFromString<AnoncredsPresentationDefinitionRequest>(presentationDefinitionRequest)

            val presentationRequest = PresentationRequest(Json.encodeToString(anoncredsPresentationDefinitionRequest))
            val presentation = Presentation(presentationSubmissionString)
            val presentationJsonObject = Json.parseToJsonElement(presentationSubmissionString).jsonObject
            if (!presentationJsonObject.containsKey("identifiers")) {
                throw Exception()
            }
            val identifiers = presentationJsonObject["identifiers"]!!.jsonArray
            val identifier = identifiers.first().jsonObject
            if (!identifier.containsKey("schema_id")) {
                throw Exception()
            }
            val schemaId = identifier["schema_id"]!!.jsonPrimitive.content
            val schema = getSchema(schemaId)

            if (!identifier.containsKey("cred_def_id")) {
                throw Exception()
            }
            val credDefId = identifier["cred_def_id"]!!.jsonPrimitive.content
            val credentialDefinition = getCredentialDefinition(credDefId)

            val schemaMap: Map<String, Schema> = mapOf(Pair(schemaId, schema))
            val credDefinition: Map<String, CredentialDefinition> = mapOf(Pair(credDefId, credentialDefinition))

            return Verifier().verifyPresentation(
                presentation = presentation,
                presReq = presentationRequest,
                schemas = schemaMap,
                credDefs = credDefinition,
                revRegDefs = null,
                revStatusLists = null,
                nonrevokeIntervalOverride = null
            )
        }
        return false
    }

    internal fun verifyJWTSignatureWithEcPublicKey(
        jwtString: String,
        ecPublicKeys: Array<ECPublicKey>
    ): Boolean {
        val jwtPartsIssuer = jwtString.split(".")
        if (jwtPartsIssuer.size != 3) {
            throw PolluxError.InvalidJWTString("Invalid JWT string, must contain 3 parts.")
        }
        val jwsObject =
            SignedJWT(
                Base64URL(jwtPartsIssuer[0]),
                Base64URL(jwtPartsIssuer[1]),
                Base64URL(jwtPartsIssuer[2])
            )
        val areVerified = ecPublicKeys.map { ecPublicKey ->
            val verifiers = ECDSAVerifier(ecPublicKey)
            val provider = BouncyCastleProviderSingleton.getInstance()
            verifiers.jcaContext.provider = provider

            jwsObject.verify(verifiers)
        }
        return areVerified.find { it } ?: false
    }

    override suspend fun extractEcPublicKeyFromVerificationMethod(coreProperty: DIDDocumentCoreProperty): Array<ECPublicKey> {
        val publicKeys = castor.getPublicKeysFromCoreProperties(arrayOf(coreProperty))

        val ecPublicKeys = publicKeys.map { publicKey ->
            when (DIDDocument.VerificationMethod.getCurveByType(publicKey.getCurve())) {
                Curve.SECP256K1 -> {
                    val kmmEcSecp = KMMECSecp256k1PublicKey.secp256k1FromBytes(publicKey.raw)
                    val x = BigInteger(1, kmmEcSecp.getCurvePoint().x)
                    val y = BigInteger(1, kmmEcSecp.getCurvePoint().y)
                    val ecPoint = ECPoint(x, y)
                    val curveName = publicKey.getCurve()
                    val sp = ECNamedCurveTable.getParameterSpec(curveName)
                    val params: ECParameterSpec =
                        ECNamedCurveSpec(sp.name, sp.curve, sp.g, sp.n, sp.h)

                    val publicKeySpec = ECPublicKeySpec(ecPoint, params)
                    val keyFactory = KeyFactory.getInstance(EC, BouncyCastleProvider())
                    keyFactory.generatePublic(publicKeySpec) as ECPublicKey
                }

                else -> {
                    throw Exception("Key type not supported ${publicKey.getCurve()}")
                }
            }
        }
        return ecPublicKeys.toTypedArray()
    }

    /**
     * Method to get the kId from the DID authentication property, Master key.
     *
     * @param did the DID to resolve
     * @return The verification method id as a string or null.
     */
    private suspend fun getSigningKid(did: DID): String? {
        val didDocHolder = castor.resolveDID(did.toString())
        val authentication = didDocHolder.coreProperties.find { property ->
            property::class == DIDDocument.Authentication::class
        }
        val verificationMethod =
            (authentication as DIDDocument.Authentication).verificationMethods.find { verificationMethod ->
                verificationMethod.id.did == did && verificationMethod.id.fragment == PrismDIDPublicKey.Usage.AUTHENTICATION_KEY.defaultId()
            }

        return verificationMethod?.id?.string()
    }
}
