package io.iohk.atala.prism.walletsdk.domain.models

data class StorableCredential(
    val id: String,
    val recoveryId: String,
    val credentialData: ByteArray,
    val issuer: String?,
    val subject: String?,
    val credentialCreated: String?,
    val credentialUpdated: String?,
    val credentialSchema: String?,
    val validUntil: String?,
    val revoked: Boolean?,
    val availableClaims: Array<String>
)
