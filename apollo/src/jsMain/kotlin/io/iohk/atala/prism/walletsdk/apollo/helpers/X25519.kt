package io.iohk.atala.prism.walletsdk.apollo.helpers

import io.iohk.atala.prism.walletsdk.domain.models.KeyPair

/**
 * X25519
 */
actual object X25519 {
    actual fun createKeyPair(): KeyPair {
        throw NotImplementedError()
    }
}
