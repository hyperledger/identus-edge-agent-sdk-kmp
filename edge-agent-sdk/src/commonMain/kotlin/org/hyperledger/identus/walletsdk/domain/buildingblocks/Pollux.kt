@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.domain.buildingblocks

// TODO: This domain interfaces cannot have dependencies to outside of domain classes
import anoncreds_uniffi.CredentialOffer
import anoncreds_uniffi.CredentialRequest
import anoncreds_uniffi.CredentialRequestMetadata
import anoncreds_uniffi.Schema
import java.security.interfaces.ECPublicKey
import kotlinx.serialization.json.JsonObject
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocumentCoreProperty
import org.hyperledger.identus.walletsdk.domain.models.PresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptions

/**
 * The `Pollux` interface represents a set of operations for working with verifiable credentials.
 */
interface Pollux {

    /**
     * Parses the given JSON data into a verifiable credential of the specified type.
     *
     * @param jsonData The JSON data representing the verifiable credential.
     * @param type The type of the verifiable credential.
     * @param linkSecret The optional link secret for the credential.
     * @param credentialMetadata The metadata for the credential request.
     * @return The parsed credential.
     */
    suspend fun parseCredential(
        jsonData: String,
        type: CredentialType,
        linkSecret: String? = null,
        credentialMetadata: CredentialRequestMetadata?
    ): Credential

    /**
     * Processes the JWT credential request and returns a string representation of the processed result.
     *
     * @param subjectDID The DID of the subject for whom the request is being processed.
     * @param privateKey The private key used for signing the JWT.
     * @param offerJson The JSON object representing the credential offer.
     * @return The string representation of the processed result.
     */
    suspend fun processCredentialRequestJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        offerJson: JsonObject
    ): String

    /**
     * Processes the SDJWT credential request and returns a string representation of the processed result.
     *
     * @param subjectDID The DID of the subject for whom the request is being processed.
     * @param privateKey The private key used for signing the JWT.
     * @param offerJson The JSON object representing the credential offer.
     * @return The string representation of the processed result.
     */
    suspend fun processCredentialRequestSDJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        offerJson: JsonObject
    ): String

    /**
     * Processes a credential request for anonymous credentials.
     *
     * @param did The DID of the subject requesting the credential.
     * @param offer The credential offer.
     * @param linkSecret The link secret for the credential.
     * @param linkSecretName The name of the link secret.
     * @return A pair containing the credential request and its metadata.
     */
    suspend fun processCredentialRequestAnoncreds(
        did: DID,
        offer: CredentialOffer,
        linkSecret: String,
        linkSecretName: String
    ): Pair<CredentialRequest, CredentialRequestMetadata>

//    /**
//     * Creates a verifiable presentation JSON Web Token (JWT) for the given subjectDID, privateKey, credential, and requestPresentationJson.
//     *
//     * @param subjectDID The DID of the subject for whom the presentation is being created.
//     * @param privateKey The private key used to sign the JWT.
//     * @param credential The credential to be included in the presentation.
//     * @param requestPresentationJson The JSON object representing the request presentation.
//     * @return The created verifiable presentation JWT.
//     */
//    fun createVerifiablePresentationJWT(
//        subjectDID: DID,
//        privateKey: PrivateKey,
//        credential: Credential,
//        requestPresentationJson: JsonObject
//    ): String
//
//    fun createVerifiablePresentationSDJWT(
//        subjectDID: DID,
//        privateKey: PrivateKey,
//        credential: Credential,
//        requestPresentationJson: JsonObject
//    ): String
//
//    suspend fun createVerifiablePresentationAnoncred(
//        request: RequestPresentation,
//        credential: AnonCredential,
//        linkSecret: LinkSecret
//    ): Presentation

    /**
     * Restores a credential using the provided restoration identifier and credential data.
     *
     * @param restorationIdentifier The restoration identifier of the credential.
     * @param credentialData The byte array containing the credential data.
     * @return The restored credential.
     */
    fun restoreCredential(restorationIdentifier: String, credentialData: ByteArray, revoked: Boolean): Credential

    /**
     * Converts a [Credential] object to a [StorableCredential] object of the specified [CredentialType].
     *
     * @param type The type of the [StorableCredential].
     * @param credential The [Credential] object to be converted.
     * @return The converted [StorableCredential].
     */
    fun credentialToStorableCredential(type: CredentialType, credential: Credential): StorableCredential

    /**
     * Extracts the credential format from the given array of attachment descriptors.
     *
     * @param formats The array of attachment descriptors.
     * @return The credential format as a CredentialType enum value.
     */
    fun extractCredentialFormatFromMessage(formats: Array<AttachmentDescriptor>): CredentialType

//    /**
//     * Retrieves the credential definition for the specified ID.
//     *
//     * @param id The ID of the credential definition.
//     * @return The credential definition.
//     */
//    suspend fun getCredentialDefinition(id: String): CredentialDefinition

    suspend fun getSchema(schemaId: String): Schema

    suspend fun createPresentationDefinitionRequest(
        type: CredentialType,
        presentationClaims: PresentationClaims,
        options: PresentationOptions
    ): String

    suspend fun createJWTPresentationSubmission(
        presentationDefinitionRequest: String,
        credential: Credential,
        privateKey: PrivateKey
    ): String

    suspend fun createAnoncredsPresentationSubmission(
        presentationDefinitionRequest: String,
        credential: Credential,
        linkSecret: String
    ): String

    suspend fun verifyPresentationSubmission(
        presentationSubmissionString: String,
        options: PresentationSubmissionOptions
    ): Boolean

    suspend fun extractEcPublicKeyFromVerificationMethod(coreProperty: DIDDocumentCoreProperty): Array<ECPublicKey>

    suspend fun isCredentialRevoked(credential: Credential): Boolean
}
