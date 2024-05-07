package org.hyperledger.identus.walletsdk.pollux.models

import anoncreds_wrapper.CredentialRequestMetadata

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
                // TODO: Remove the `replace` when anoncreds-kmp 0.4.3 is published. 0.4.2 has an issue where getLinkSecretName returns a string with double quotation.
                linkSecretName = metadata.getLinkSecretName().replace("\"", ""),
                json = metadata.getJson()
            )
        }
    }
}
