package org.hyperledger.identus.walletsdk.pollux.models

import anoncreds_uniffi.CredentialRequestMetadata
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * Represents the metadata for a credential request.
 *
 * @property linkSecretName The name of the link secret.
 * @property json The json string from the wrapper credential request metadata.
 */
data class CredentialRequestMeta(
    var linkSecretName: String,
    var json: String
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
                linkSecretName = Json.parseToJsonElement(metadata.toJson()).jsonObject["link_secret_name"]?.toString()
                    ?.replace("\"", "") ?: "",
                json = metadata.toJson()
            )
        }
    }
}
