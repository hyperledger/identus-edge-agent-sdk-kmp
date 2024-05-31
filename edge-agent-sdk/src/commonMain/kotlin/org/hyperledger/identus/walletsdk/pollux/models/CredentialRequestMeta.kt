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

    /**
     * Converts the given JSON string to a RustCredentialRequestMetadata object.
     *
     * @return The RustCredentialsRequestMetadata object created from the JSON string.
     */
    fun toRustCredentialRequestMetadata(): CredentialRequestMetadata {
        return CredentialRequestMetadata(json)
    }

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
                linkSecretName = metadata.getLinkSecretName().replace("\"", ""),
                json = metadata.getJson()
            )
        }
    }
}
