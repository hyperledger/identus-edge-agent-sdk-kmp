package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

interface KeyRestoration {

    fun isPrivateKeyData(identifier: String, data: ByteArray): Boolean

    fun isPublicKeyData(identifier: String, data: ByteArray): Boolean

    fun restorePrivateKey(key: StorableKey): PrivateKey

    fun restorePublicKey(key: StorableKey): PublicKey
}
