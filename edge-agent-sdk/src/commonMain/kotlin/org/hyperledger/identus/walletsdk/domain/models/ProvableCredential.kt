package org.hyperledger.identus.walletsdk.domain.models

import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey

sealed class CredentialOperationsOptions {
    data class Schema(val id: String, val json: String) : CredentialOperationsOptions()
    data class SchemaDownloader(val api: Api) : CredentialOperationsOptions()
    data class CredentialDefinition(val id: String, val json: String) : CredentialOperationsOptions()
    data class CredentialDefinitionDownloader(val api: Api) : CredentialOperationsOptions()
    data class LinkSecret(val id: String, val secret: String) : CredentialOperationsOptions()
    data class SubjectDID(val did: DID) : CredentialOperationsOptions()
    data class Entropy(val entropy: String) : CredentialOperationsOptions()
    data class SignableKey(val key: SignableKey?) : CredentialOperationsOptions()
    data class ExportableKey(val key: PrivateKey?) : CredentialOperationsOptions()
    data class ZkpPresentationParams(val attributes: Map<String, Boolean>, val predicates: List<String>) :
        CredentialOperationsOptions()

    data class DisclosingClaims(val claims: List<String>) : CredentialOperationsOptions()
    data class Custom(val key: String, val data: ByteArray) : CredentialOperationsOptions()
}

interface ProvableCredential {
    suspend fun presentation(request: ByteArray, options: List<CredentialOperationsOptions>): String
}
