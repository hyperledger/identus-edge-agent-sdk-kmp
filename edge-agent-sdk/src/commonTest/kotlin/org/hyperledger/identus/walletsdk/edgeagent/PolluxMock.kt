package org.hyperledger.identus.walletsdk.edgeagent

import anoncreds_wrapper.CredentialOffer
import anoncreds_wrapper.CredentialRequestMetadata
import anoncreds_wrapper.LinkSecret
import anoncreds_wrapper.Presentation
import anoncreds_wrapper.Schema
import kotlinx.serialization.json.JsonObject
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pollux
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocumentCoreProperty
import org.hyperledger.identus.walletsdk.domain.models.PresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationDefinitionRequest
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmission
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.RequestPresentation
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.CredentialRequest
import org.hyperledger.identus.walletsdk.pollux.models.CredentialRequestMeta
import java.security.interfaces.ECPublicKey

class PolluxMock : Pollux {

    var extractedCredentialFormatFromMessageReturn: CredentialType? = null
    var processCredentialRequestAnoncredsReturn: Pair<CredentialRequest, CredentialRequestMeta>? = null

    override suspend fun parseCredential(
        jsonData: String,
        type: CredentialType,
        linkSecret: LinkSecret?,
        credentialMetadata: CredentialRequestMetadata?
    ): Credential {
        TODO("Not yet implemented")
    }

    override fun processCredentialRequestJWT(subjectDID: DID, privateKey: PrivateKey, offerJson: JsonObject): String {
        TODO("Not yet implemented")
    }

    override fun createVerifiablePresentationJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        credential: Credential,
        requestPresentationJson: JsonObject
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun createVerifiablePresentationAnoncred(
        request: RequestPresentation,
        credential: AnonCredential,
        linkSecret: LinkSecret
    ): Presentation {
        TODO("Not yet implemented")
    }

    override fun restoreCredential(
        restorationIdentifier: String,
        credentialData: ByteArray,
        revoked: Boolean
    ): Credential {
        TODO("Not yet implemented")
    }

    override suspend fun processCredentialRequestAnoncreds(
        did: DID,
        offer: CredentialOffer,
        linkSecret: LinkSecret,
        linkSecretName: String
    ): Pair<anoncreds_wrapper.CredentialRequest, CredentialRequestMetadata> {
        TODO("Not yet implemented")
        // return processCredentialRequestAnoncredsReturn ?: throw Exception("Return not defined")
    }

    override fun credentialToStorableCredential(type: CredentialType, credential: Credential): StorableCredential {
        TODO("Not yet implemented")
    }

    override fun extractCredentialFormatFromMessage(formats: Array<AttachmentDescriptor>): CredentialType {
        return extractedCredentialFormatFromMessageReturn ?: throw Exception("Return not defined")
    }

    override suspend fun getCredentialDefinition(id: String): anoncreds_wrapper.CredentialDefinition {
        TODO("Not yet implemented")
    }

    override suspend fun getSchema(schemaId: String): Schema {
        TODO("Not yet implemented")
    }

    override suspend fun createPresentationDefinitionRequest(
        type: CredentialType,
        presentationClaims: PresentationClaims,
        options: PresentationOptions
    ): PresentationDefinitionRequest {
        TODO("Not yet implemented")
    }

    override suspend fun createPresentationSubmission(
        presentationDefinitionRequest: PresentationDefinitionRequest,
        credential: Credential,
        privateKey: PrivateKey
    ): PresentationSubmission {
        TODO("Not yet implemented")
    }

    override suspend fun verifyPresentationSubmission(
        presentationSubmission: PresentationSubmission,
        options: PresentationSubmissionOptions
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun extractEcPublicKeyFromVerificationMethod(coreProperty: DIDDocumentCoreProperty): Array<ECPublicKey> {
        TODO("Not yet implemented")
    }

    override suspend fun isCredentialRevoked(credential: Credential): Boolean {
        TODO("Not yet implemented")
    }
}
