package io.iohk.atala.prism.walletsdk.pollux.models

import anoncreds_wrapper.CredentialRequestMetadata
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class CredentialRequestMeta(
    var linkSecretBlindingData: LinkSecretBlindingData,
    var linkSecretName: String,
    var nonce: String
) {
    companion object {
        fun fromCredentialRequestMetadata(metadata: CredentialRequestMetadata): CredentialRequestMeta {
            return CredentialRequestMeta(
                linkSecretName = metadata.linkSecretName,
                linkSecretBlindingData = Json.decodeFromString(metadata.linkSecretBlindingData),
                nonce = metadata.nonce.getValue()
            )
        }
    }
}
