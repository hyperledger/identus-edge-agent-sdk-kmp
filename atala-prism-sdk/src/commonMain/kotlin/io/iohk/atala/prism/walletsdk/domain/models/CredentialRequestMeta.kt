package io.iohk.atala.prism.walletsdk.domain.models

import anoncreds_wrapper.CredentialRequestMetadata
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class CredentialRequestMeta(
    var linkSecretBlindingData: LinkSecretBlindingData,
    var linkSecretName: String,
    var nonce: String,
) {
    companion object {
        fun fromCredentialRequestMetadata(metadata: CredentialRequestMetadata): CredentialRequestMeta {
            return CredentialRequestMeta(
                linkSecretName = metadata.linkSecretName,
                linkSecretBlindingData = Json.decodeFromString(metadata.linkSecretBlindingData),
                nonce = metadata.nonce // TODO: How to transform `Nonce` to String or any other data type to store it into pluto?
            )
        }
    }
}
