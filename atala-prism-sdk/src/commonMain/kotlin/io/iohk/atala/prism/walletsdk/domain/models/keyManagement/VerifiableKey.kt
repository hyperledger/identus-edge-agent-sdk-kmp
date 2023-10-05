package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

interface VerifiableKey {
    fun verify(message: ByteArray, signature: ByteArray): Boolean
}
