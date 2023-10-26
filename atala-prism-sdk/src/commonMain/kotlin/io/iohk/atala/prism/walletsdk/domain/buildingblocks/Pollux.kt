package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import anoncreds_wrapper.CredentialDefinition
import anoncreds_wrapper.CredentialOffer
import anoncreds_wrapper.CredentialRequest
import anoncreds_wrapper.CredentialRequestMetadata
import anoncreds_wrapper.LinkSecret
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import kotlinx.serialization.json.JsonObject

interface Pollux {
    suspend fun parseCredential(
        data: String,
        type: CredentialType,
        linkSecret: LinkSecret? = null,
        credentialMetadata: CredentialRequestMetadata?
    ): Credential

    fun processCredentialRequestJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        offerJson: JsonObject
    ): String

    suspend fun processCredentialRequestAnoncreds(
        did: DID,
        offer: CredentialOffer,
        linkSecret: LinkSecret,
        linkSecretName: String
    ): Pair<CredentialRequest, CredentialRequestMetadata>

    fun createVerifiablePresentationJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        credential: Credential,
        requestPresentationJson: JsonObject
    ): String

    fun restoreCredential(restorationIdentifier: String, credentialData: ByteArray): Credential

    fun credentialToStorableCredential(type: CredentialType, credential: Credential): StorableCredential

    fun extractCredentialFormatFromMessage(formats: Array<AttachmentDescriptor>): CredentialType

    suspend fun getCredentialDefinition(id: String): CredentialDefinition
}
