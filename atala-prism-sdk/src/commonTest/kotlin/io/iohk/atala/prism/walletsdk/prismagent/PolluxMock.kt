package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
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

class PolluxMock : Pollux {

    var extractedCredentialFormatFromMessageReturn: CredentialType? = null
    var processCredentialRequestAnoncredsReturn: Pair<CredentialRequest, CredentialRequestMeta>? = null

    override fun parseCredential(
        data: String,
        type: CredentialType,
        linkSecret: String?,
        credentialMetadata: CredentialRequestMeta?
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

    override fun restoreCredential(restorationIdentifier: String, credentialData: ByteArray): Credential {
        TODO("Not yet implemented")
    }

    override fun processCredentialRequestAnoncreds(
        offer: OfferCredential,
        linkSecret: String,
        linkSecretName: String
    ): Pair<CredentialRequest, CredentialRequestMeta> {
        return processCredentialRequestAnoncredsReturn ?: throw Exception("Return not defined")
    }

    override fun credentialToStorableCredential(type: CredentialType, credential: Credential): StorableCredential {
        TODO("Not yet implemented")
    }

    override fun extractCredentialFormatFromMessage(formats: Array<CredentialFormat>): CredentialType {
        return extractedCredentialFormatFromMessageReturn ?: throw Exception("Return not defined")
    }

    override fun getCredentialDefinition(id: String): CredentialDefinition {
        TODO("Not yet implemented")
    }
}
