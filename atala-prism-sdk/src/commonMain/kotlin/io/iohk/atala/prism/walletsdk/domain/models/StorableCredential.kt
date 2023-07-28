package io.iohk.atala.prism.walletsdk.domain.models

interface StorableCredential : Credential {
    val recoveryId: String
    val credentialData: ByteArray
    val credentialCreated: String?
    val credentialUpdated: String?
    val credentialSchema: String?
    val validUntil: String?
    val revoked: Boolean?
    val availableClaims: Array<String>
}
