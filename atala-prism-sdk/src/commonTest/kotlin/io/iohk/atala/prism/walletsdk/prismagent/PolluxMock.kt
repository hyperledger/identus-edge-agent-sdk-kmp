package io.iohk.atala.prism.walletsdk.prismagent

import anoncreds_wrapper.CredentialOffer
import anoncreds_wrapper.CredentialRequestMetadata
import anoncreds_wrapper.LinkSecret
import anoncreds_wrapper.Presentation
import anoncreds_wrapper.Schema
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.pollux.models.AnonCredential
import io.iohk.atala.prism.walletsdk.pollux.models.CredentialRequest
import io.iohk.atala.prism.walletsdk.pollux.models.CredentialRequestMeta
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.PresentationDefinitionRequest
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.PresentationOptions
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.PresentationSubmission
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.ProofTypes
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.RequestPresentation
import kotlinx.serialization.json.JsonObject

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

    override fun restoreCredential(restorationIdentifier: String, credentialData: ByteArray, revoked: Boolean): Credential {
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
        proofs: Array<ProofTypes>,
        options: PresentationOptions
    ): PresentationDefinitionRequest {
        TODO("Not yet implemented")
    }

    override suspend fun createPresentationSubmission(
        presentationDefinitionRequest: PresentationDefinitionRequest,
        credential: Credential,
        did: DID,
        privateKey: PrivateKey
    ): PresentationSubmission {
        TODO("Not yet implemented")
    }

    override suspend fun verifyPresentationSubmissionJWT(jwt: String, publicKey: PublicKey): Boolean {
        TODO("Not yet implemented")
    }
}
