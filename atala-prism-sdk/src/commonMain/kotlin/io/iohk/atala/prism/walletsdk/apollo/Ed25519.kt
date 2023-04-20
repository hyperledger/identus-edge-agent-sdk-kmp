package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.KeyPair

/**
 * Ed25519 is a variation of EdDSA
 * TODO(Future Moussa -> Use Apollo instead)
 */
expect object Ed25519 {
    fun createKeyPair(): KeyPair
}
