package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

interface SignableKey {
    fun sign(message: ByteArray): ByteArray
}
