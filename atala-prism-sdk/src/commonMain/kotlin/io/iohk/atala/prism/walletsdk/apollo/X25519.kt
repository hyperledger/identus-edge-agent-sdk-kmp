package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.KeyPair

/**
 * X25519
 */
expect object X25519 {

    /**
     * Create keypair for [X25519].
     *
     * @return [KeyPair] for [X25519].
     */
    fun createKeyPair(): KeyPair
}
