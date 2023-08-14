package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.CredentialDefinition
import io.iohk.atala.prism.walletsdk.domain.models.CredentialRequest
import io.iohk.atala.prism.walletsdk.domain.models.CredentialRequestMeta
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.CredentialFormat
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.OfferCredential
import kotlinx.serialization.json.JsonObject

interface Pollux {
    fun parseCredential(
        data: String,
        type: CredentialType,
        linkSecret: String? = null,
        credentialMetadata: CredentialRequestMeta?
    ): Credential

    fun processCredentialRequestJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        offerJson: JsonObject,
    ): String

    fun processCredentialRequestAnoncreds(
        offer: OfferCredential,
        linkSecret: String,
        linkSecretName: String
    ): Pair<CredentialRequest, CredentialRequestMeta>

    fun createVerifiablePresentationJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        credential: Credential,
        requestPresentationJson: JsonObject
    ): String

    fun restoreCredential(restorationIdentifier: String, credentialData: ByteArray): Credential

    fun credentialToStorableCredential(type: CredentialType, credential: Credential): StorableCredential

    fun extractCredentialFormatFromMessage(formats: Array<CredentialFormat>): CredentialType

    fun getCredentialDefinition(id: String): CredentialDefinition
}
