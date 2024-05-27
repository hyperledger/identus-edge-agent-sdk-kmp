@file:Suppress("ktlint:standard:import-ordering", "ktlint:standard:wrapping")

package org.hyperledger.identus.walletsdk.pollux

import anoncreds_wrapper.CredentialDefinition
import anoncreds_wrapper.CredentialDefinitionId
import anoncreds_wrapper.CredentialOffer
import anoncreds_wrapper.CredentialRequest
import anoncreds_wrapper.CredentialRequestMetadata
import anoncreds_wrapper.CredentialRequests
import anoncreds_wrapper.LinkSecret
import anoncreds_wrapper.Presentation
import anoncreds_wrapper.PresentationRequest
import anoncreds_wrapper.Prover
import anoncreds_wrapper.RequestedAttribute
import anoncreds_wrapper.RequestedPredicate
import anoncreds_wrapper.Schema
import anoncreds_wrapper.SchemaId
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PublicKey
import io.iohk.atala.prism.apollo.utils.KMMEllipticCurve
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptionsJWT
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.didcommx.didcomm.common.Typ
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pollux
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.ApiImpl
import org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.domain.models.httpClient
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationDefinitionRequest
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.RequestPresentation
import org.hyperledger.identus.walletsdk.edgeagent.shared.KeyValue
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.hyperledger.identus.walletsdk.pollux.models.W3CCredential
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPrivateKeySpec
import java.security.spec.ECPublicKeySpec
import java.util.*
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDDocumentCoreProperty
import org.hyperledger.identus.walletsdk.domain.models.InputFieldFilter
import org.hyperledger.identus.walletsdk.domain.models.PresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SignableKey
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.DescriptorItemFormat
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmission

/**
 * Class representing the implementation of the Pollux interface.
 *
 * @property castor An API object for interacting with the Castor system.
 */
@Suppress("LABEL_NAME_CLASH")
class PolluxImpl(
    val castor: Castor,
    private val api: Api = ApiImpl(httpClient())
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
        linkSecret: LinkSecret?,
        credentialMetadata: CredentialRequestMetadata?
    ): Credential {
        return when (type) {
            CredentialType.JWT -> {
                JWTCredential.fromJwtString(jsonData)
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
            "jwt+credential" -> {
                cred = JWTCredential.fromJwtString(credentialData.decodeToString())
            }

            "anon+credential" -> {
                cred = AnonCredential.fromStorableData(credentialData)
            }

            "w3c+credential" -> {
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

    /**
     * Converts the map of [anoncreds_wrapper.AttributeInfoValue] values to a list of [RequestedAttribute].
     *
     * @return The list of [RequestedAttribute].
     */
    private fun Map<String, anoncreds_wrapper.AttributeInfoValue>.toListRequestedAttribute(): List<RequestedAttribute> {
        return this.keys.toList().map {
            RequestedAttribute(
                referent = it,
                revealed = true
            )
        }
    }

    /**
     * Converts the map of [anoncreds_wrapper.PredicateInfoValue] values to a list of [RequestedPredicate].
     *
     * @receiver The map of [anoncreds_wrapper.PredicateInfoValue] values.
     * @return The list of [RequestedPredicate].
     */
    private fun Map<String, anoncreds_wrapper.PredicateInfoValue>.toListRequestedPredicate(): List<RequestedPredicate> {
        return this.keys.toList().map {
            RequestedPredicate(it)
        }
    }

    override suspend fun createVerifiablePresentationAnoncred(
        request: RequestPresentation,
        credential: AnonCredential,
        linkSecret: LinkSecret
    ): Presentation {
        if (request.attachments.isEmpty()) {
            throw PolluxError.RequestPresentationHasWrongAttachments("Attachment cannot be empty")
        }
        if (request.attachments[0].data !is AttachmentBase64) {
            throw PolluxError.RequestPresentationHasWrongAttachments("Attachment data must be AttachmentBase64")
        }
        val attachmentBase64 = request.attachments[0].data as AttachmentBase64
        val presentationRequest = PresentationRequest(attachmentBase64.base64.base64UrlDecoded)
        val cred = anoncreds_wrapper.Credential(credential.id)

        val requestedAttributes =
            presentationRequest.getRequestedAttributes().toListRequestedAttribute()
        val requestedPredicate =
            presentationRequest.getRequestedPredicates().toListRequestedPredicate()

        val credentialRequests = CredentialRequests(
            credential = cred,
            requestedAttribute = requestedAttributes,
            requestedPredicate = requestedPredicate
        )
        val schema = getSchema(credential.schemaID)

        val schemaId = credential.schemaID
        val schemaMap: Map<SchemaId, Schema> = mapOf(Pair(schemaId, schema))

        val credentialDefinition = getCredentialDefinition(credential.credentialDefinitionID)
        val credDefinition: Map<CredentialDefinitionId, CredentialDefinition> = mapOf(
            Pair(credential.credentialDefinitionID, credentialDefinition)
        )

        return Prover().createPresentation(
            presentationRequest = presentationRequest,
            credentials = listOf(credentialRequests),
            selfAttested = null,
            linkSecret = linkSecret,
            schemas = schemaMap,
            credentialDefinitions = credDefinition
        )
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

    /**
     * Retrieves the credential definition for the specified ID.
     *
     * @param id The ID of the credential definition.
     * @return The credential definition.
     */
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

    override suspend fun getSchema(schemaId: String): Schema {
        val result = api.request(
            HttpMethod.Get.value,
            schemaId,
            emptyArray(),
            arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
            null
        )

        if (result.status == 200) {
            val schema = (Json.parseToJsonElement(result.jsonString) as JsonObject)
            if (schema.containsKey("attrNames") && schema.containsKey("issuerId")) {
                val name = schema["name"]?.jsonPrimitive?.content
                val version = schema["version"]?.jsonPrimitive?.content
                val attrs = schema["attrNames"]
                val attrNames = attrs?.jsonArray?.map { value -> value.jsonPrimitive.content }
                val issuerId =
                    schema["issuerId"]?.jsonPrimitive?.content
                return Schema(
                    name = name ?: throw PolluxError.InvalidCredentialError(),
                    version = version ?: throw PolluxError.InvalidCredentialError(),
                    attrNames = attrNames ?: throw PolluxError.InvalidCredentialError(),
                    issuerId = issuerId ?: throw PolluxError.InvalidCredentialError()
                )
            }
        }
        throw PolluxError.InvalidCredentialDefinitionError()
    }

    /**
     * Parses a PrivateKey into an ECPrivateKey.
     *
     * @param privateKey The PrivateKey to parse.
     * @return The parsed ECPrivateKey.
     */
    internal fun parsePrivateKey(privateKey: PrivateKey): ECPrivateKey {
        val curveName = KMMEllipticCurve.SECP256k1.value
        val sp = ECNamedCurveTable.getParameterSpec(curveName)
        val params: ECParameterSpec = ECNamedCurveSpec(sp.name, sp.curve, sp.g, sp.n, sp.h)
        val privateKeySpec = ECPrivateKeySpec(BigInteger(1, privateKey.getValue()), params)
        val keyFactory = KeyFactory.getInstance(EC, BouncyCastleProvider())
        return keyFactory.generatePrivate(privateKeySpec) as ECPrivateKey
    }

    private fun parsePublicKey(publicKey: PublicKey): ECPublicKey {
        val curveName = KMMEllipticCurve.SECP256k1.value
        val sp = ECNamedCurveTable.getParameterSpec(curveName)
        val params: ECParameterSpec = ECNamedCurveSpec(sp.name, sp.curve, sp.g, sp.n, sp.h)

        val publicKeySpec = ECPublicKeySpec(params.generator, params)
        val keyFactory = KeyFactory.getInstance(EC, BouncyCastleProvider())
        return keyFactory.generatePublic(publicKeySpec) as ECPublicKey
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
    private fun signClaimsRequestCredentialJWT(
        subjectDID: DID,
        privateKey: ECPrivateKey,
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
    internal fun signClaimsProofPresentationJWT(
        subjectDID: DID,
        privateKey: ECPrivateKey,
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
    private fun signClaims(
        subjectDID: DID,
        privateKey: ECPrivateKey,
        domain: String,
        challenge: String,
        credential: Credential? = null
    ): String {
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

    override suspend fun createPresentationDefinitionRequest(
        type: CredentialType,
        presentationClaims: PresentationClaims,
        options: PresentationOptions
    ): PresentationDefinitionRequest {
        if (type != CredentialType.JWT) {
            throw PolluxError.CredentialTypeNotSupportedError()
        }
        val jwt = options.jwt
        if (jwt.isEmpty()) {
            throw PolluxError.InvalidJWTPresentationDefinitionError("Presentation option must contain at least one valid JWT alg that is not empty.")
        }
        val paths = presentationClaims.claims.keys
        val mutableListFields: MutableList<PresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.Field> =
            paths.map { path ->
                PresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.Field(
                    path = arrayOf("$.vc.credentialSubject.$path", "$.credentialSubject.$path"),
                    id = UUID.randomUUID().toString(),
                    optional = false,
                    filter = presentationClaims.claims[path],
                    name = path,
                )
            } as MutableList

        presentationClaims.issuer?.let { issuer ->
            mutableListFields.add(
                PresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.Field(
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

        val constraints = PresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints(
            fields = mutableListFields.toTypedArray(),
            limitDisclosure = PresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.LimitDisclosure.REQUIRED
        )

        val format =
            PresentationDefinitionRequest.PresentationDefinition.InputDescriptor.PresentationFormat(
                jwt = jwt.let {
                    PresentationDefinitionRequest.PresentationDefinition.InputDescriptor.JwtFormat(
                        jwt.toList()
                    )
                }
            )

        val inputDescriptor = PresentationDefinitionRequest.PresentationDefinition.InputDescriptor(
            name = options.name,
            purpose = options.purpose,
            constraints = constraints,
            format = format
        )

        return PresentationDefinitionRequest(
            presentationDefinition = PresentationDefinitionRequest.PresentationDefinition(
                inputDescriptors = arrayOf(inputDescriptor),
                format = format
            ),
            options = PresentationDefinitionRequest.PresentationDefinitionOptions(
                domain = options.domain,
                challenge = options.challenge
            )
        )
    }

    override suspend fun createPresentationSubmission(
        presentationDefinitionRequest: PresentationDefinitionRequest,
        credential: Credential,
        privateKey: PrivateKey
    ): PresentationSubmission {
        if (credential::class != JWTCredential::class) {
            throw PolluxError.CredentialTypeNotSupportedError()
        }
        if (privateKey::class != Secp256k1PrivateKey::class) {
            throw PolluxError.PrivateKeyTypeNotSupportedError()
        }
        privateKey as Secp256k1PrivateKey
        credential as JWTCredential
        val descriptorItems =
            presentationDefinitionRequest.presentationDefinition.inputDescriptors.map { inputDescriptor ->
                if (inputDescriptor.format != null && (inputDescriptor.format.jwt == null || inputDescriptor.format.jwt.alg.isEmpty())) {
                    throw Exception("Invalid format") // TODO: Custom exception
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
                privateKey.sign(presentationDefinitionRequest.options.challenge.encodeToByteArray())

            val ecPrivateKey = parsePrivateKey(privateKey)
            val presentationJwt = signClaimsProofPresentationJWT(
                subjectDID = DID(subject),
                privateKey = ecPrivateKey,
                credential = credential,
                domain = presentationDefinitionRequest.options.domain,
                challenge = presentationDefinitionRequest.options.challenge
            )

            return PresentationSubmission(
                presentationSubmission = PresentationSubmission.Submission(
                    definitionId = presentationDefinitionRequest.presentationDefinition.id
                        ?: UUID.randomUUID().toString(),
                    descriptorMap = descriptorItems
                ),
                verifiablePresentation = arrayOf(presentationJwt)
            )
        } ?: throw PolluxError.NullField("CredentialSubject")
    }

    override suspend fun verifyPresentationSubmission(
        presentationSubmission: PresentationSubmission,
        options: PresentationSubmissionOptions
    ): Boolean {
        if (options::class == PresentationSubmissionOptionsJWT::class) {
            val presentationDefinitionRequest =
                (options as PresentationSubmissionOptionsJWT).presentationDefinitionRequest
            presentationDefinitionRequest.presentationDefinition
            val inputDescriptors =
                presentationDefinitionRequest.presentationDefinition.inputDescriptors
            val descriptorMap = DescriptorPath(Json.encodeToJsonElement(presentationSubmission))
            val descriptorMaps = presentationSubmission.presentationSubmission.descriptorMap
            descriptorMaps.forEach { descriptorItem ->
                if (descriptorItem.format != DescriptorItemFormat.JWT_VP.value) {
                    throw PolluxError.VerificationUnsuccessful("Invalid submission, ${descriptorItem.path} expected to have format ${DescriptorItemFormat.JWT_VP.value}")
                }

                var newPath: String? = null
                if (!descriptorItem.path.contains("verifiablePresentation")) {
                    newPath = PresentationSubmission.Submission.DescriptorItem.replacePathWithVerifiablePresentation(
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
                        if (verifiableCredential.subject != issuer) {
                            throw PolluxError.VerificationUnsuccessful("Invalid submission,")
                        }

                        val didDocHolder = castor.resolveDID(verifiableCredential.issuer)
                        val authenticationMethodHolder =
                            didDocHolder.coreProperties.find { it::class == DIDDocument.Authentication::class }
                                ?: throw PolluxError.VerificationUnsuccessful("Holder core properties must contain Authentication")
                        val ecPublicKeysHolder =
                            extractEcPublicKeyFromVerificationMethod(authenticationMethodHolder)

                        if (!verifyJWTSignatureWithEcPublicKey(verifiableCredential.id, ecPublicKeysHolder)) {
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
                            if (constraints.limitDisclosure == PresentationDefinitionRequest.PresentationDefinition.InputDescriptor.Constraints.LimitDisclosure.REQUIRED) {
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
                                                    reason = "Input field filter for ${field.name} is null"
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
        } else {
            // TODO: Anoncreds and more
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
}
