package io.iohk.atala.prism.walletsdk.pollux.models

import anoncreds_wrapper.CredentialRequestMetadata
import kotlinx.serialization.json.Json

/**
 * Represents the metadata for a credential request.
 *
 * @property linkSecretBlindingData The blinding data used in the Link-Secret protocol.
 * @property linkSecretName The name of the link secret.
 * @property nonce The nonce value.
 */
data class CredentialRequestMeta(
    var linkSecretBlindingData: LinkSecretBlindingData,
    var linkSecretName: String,
    var nonce: String
) {
    companion object {
        /**
         * Converts a [CredentialRequestMetadata] object into a [CredentialRequestMeta] object.
         *
         * @param metadata The [CredentialRequestMetadata] object to convert.
         * @return The converted [CredentialRequestMeta] object.
         */
        @JvmStatic
        fun fromCredentialRequestMetadata(metadata: CredentialRequestMetadata): CredentialRequestMeta {
            return CredentialRequestMeta(
                linkSecretName = metadata.getLinkSecretName(),
                linkSecretBlindingData = Json.decodeFromString(metadata.getLinkSecretBlindingData()),
                nonce = metadata.getNonce().getValue()
            )
        }
    }
}
