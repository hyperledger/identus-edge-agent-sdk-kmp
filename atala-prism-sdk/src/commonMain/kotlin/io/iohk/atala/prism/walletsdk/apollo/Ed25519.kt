package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Signature

/**
 * Ed25519 is a variation of EdDSA
 */
expect object Ed25519 {
    fun createKeyPair(): KeyPair

    fun sign(privateKey: PrivateKey, message: ByteArray): ByteArray

    fun verify(publicKey: PublicKey, signature: Signature, message: ByteArray): Boolean
}
