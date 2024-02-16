@file:Suppress("ktlint:standard:import-ordering")

package io.iohk.atala.prism.walletsdk.pollux

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
import io.iohk.atala.prism.apollo.utils.KMMEllipticCurve
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PrivateKey
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.Api
import io.iohk.atala.prism.walletsdk.domain.models.ApiImpl
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.PolluxError
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.domain.models.httpClient
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.pollux.models.AnonCredential
import io.iohk.atala.prism.walletsdk.pollux.models.JWTCredential
import io.iohk.atala.prism.walletsdk.pollux.models.W3CCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.PresentationDefinitionRequest
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.PresentationDefinitionRequest.PresentationDefinitionBody.InputDescriptor
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.PresentationOptions
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.PresentationSubmission
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.Proof
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.ProofTypes
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.RequestPresentation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.W3cCredentialSubmission
import io.iohk.atala.prism.walletsdk.prismagent.shared.KeyValue
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.util.date.getTimeMillis
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECParameterSpec
import java.security.spec.ECPrivateKeySpec
import java.security.spec.ECPublicKeySpec
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.didcommx.didcomm.common.Typ

/**
 * Class representing the implementation of the Pollux interface.
 *
 * @property castor An API object for interacting with the Castor system.
 */
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
            JWTCredential(data)
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
                cred = JWTCredential(credentialData.decodeToString())
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
        if (request.attachments.isEmpty() || request.attachments[0].data !is AttachmentBase64) {
            throw Error("")
            // TODO: Custom pollux error
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
    private fun parsePrivateKey(privateKey: PrivateKey): ECPrivateKey {
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
    private fun signClaimsProofPresentationJWT(
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
        val vp: MutableMap<String, Collection<String>> = mutableMapOf(
            CONTEXT to setOf(CONTEXT_URL),
            TYPE to setOf(VERIFIABLE_PRESENTATION)
        )
        credential?.let {
            vp[VERIFIABLE_CREDENTIAL] = listOf(it.id)
        }
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

    override suspend fun createPresentationDefinitionRequest(
        type: CredentialType,
        proofs: Array<ProofTypes>,
        options: PresentationOptions
    ): PresentationDefinitionRequest {
        if (type != CredentialType.JWT) {
            throw PolluxError.CredentialTypeNotSupportedError()
        }
        val jwt = options.jwtAlg
        val jwtVc = options.jwtVcAlg
        val jwtVp = options.jwtVpAlg
        if (jwt == null && jwtVc == null && jwtVp == null) {
            throw PolluxError.InvalidJWTPresentationDefinitionError("Presentation option must contain at least one valid JWT alg.")
        }

        val path = proofs
            .flatMap { proofType ->
                (proofType.requiredFields?.toList() ?: emptyList()) + (proofType.trustIssuers?.toList() ?: emptyList())
            }

        val constraints =
            InputDescriptor.Constraints(
                fields = arrayOf(
                    InputDescriptor.Constraints.Fields(
                        path = path.toTypedArray()
                    )
                )
            )

        val inputDescriptor = InputDescriptor(
            name = options.name,
            purpose = options.purpose,
            constraints = constraints
        )

        val format =
            InputDescriptor.PresentationFormat(
                jwtVc = jwtVc?.let {
                    InputDescriptor.JwtVcFormat(
                        jwtVc.toList()
                    )
                },
                jwtVp = jwtVp?.let {
                    InputDescriptor.JwtVpFormat(
                        jwtVp.toList()
                    )
                }
            )

        return PresentationDefinitionRequest(
            PresentationDefinitionRequest.PresentationDefinitionBody(
                inputDescriptors = arrayOf(inputDescriptor),
                format = format
            ),
            challenge = options.challenge,
            domain = options.domain
        )
    }

    override suspend fun createPresentationSubmission(
        presentationDefinitionRequest: PresentationDefinitionRequest,
        credential: Credential,
        did: DID,
        privateKey: PrivateKey
    ): PresentationSubmission {
        if (credential::class != JWTCredential::class) {
            throw PolluxError.CredentialTypeNotSupportedError()
        }
        if (privateKey::class != Secp256k1PrivateKey::class) {
            throw PolluxError.PrivateKeyTypeNotSupportedError()
        }
        privateKey as Secp256k1PrivateKey
        val descriptorItems =
            presentationDefinitionRequest.presentationDefinitionBody.inputDescriptors.map { inputDescriptor ->
                PresentationSubmission.Submission.DescriptorItem(
                    id = inputDescriptor.id,
                    format = "jwt_vp",
                    path = "$.verifiableCredential[0]"
                )
            }.toTypedArray()

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault())

        val jws = credential.id
        credential.subject?.let { subject ->
            presentationDefinitionRequest.challenge?.let { challenge ->

                val didDoc = castor.resolveDID(subject)
                val authenticationProperty = didDoc.coreProperties.find { property ->
                    property::class == DIDDocument.Authentication::class
                } as DIDDocument.Authentication

                val proof = Proof(
                    type = "EcdsaSecp256k1Signature2019",
                    created = dateFormat.format(getTimeMillis() * 1000L),
                    proofPurpose = Proof.Purpose.AUTHENTICATION.value,
                    verificationMethod = authenticationProperty.verificationMethods.first().id.string(),
                    challenge = privateKey.sign(challenge.encodeToByteArray())
                        .toHexString(),
                    domain = presentationDefinitionRequest.domain,
                    jws = jws
                )

                return PresentationSubmission(
                    presentationSubmission = PresentationSubmission.Submission(
                        definitionId = presentationDefinitionRequest.presentationDefinitionBody.id
                            ?: UUID.randomUUID().toString(),
                        descriptorMap = descriptorItems
                    ),
                    verifiableCredential = arrayOf(
                        W3cCredentialSubmission(vc = (credential as JWTCredential).jwtPayload.verifiableCredential)
                    ),
                    proof = proof
                )
            } ?: throw PolluxError.NoDomainOrChallengeFound()
        } ?: throw PolluxError.RequestMissingField("subject")
    }

    override suspend fun verifyPresentationSubmissionJWT(jwt: String, publicKey: PublicKey): Boolean {
        val ecPublicKey = parsePublicKey(publicKey)
        val jwtParts = jwt.split(".")
        val jwsObject = SignedJWT(Base64URL(jwtParts[0]), Base64URL(jwtParts[1]), Base64URL(jwtParts[2]))

        val verifiers = ECDSAVerifier(ecPublicKey)

        return jwsObject.verify(verifiers)
    }
}
