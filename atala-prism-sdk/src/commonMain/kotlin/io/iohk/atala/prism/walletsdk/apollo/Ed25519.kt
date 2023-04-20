package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.KeyPair

/**
 * Ed25519 is a variation of EdDSA
 */
expect object Ed25519 {
    fun createKeyPair(): KeyPair
}
