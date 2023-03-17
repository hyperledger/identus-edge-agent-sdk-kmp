package io.iohk.atala.prism.walletsdk.apollo.helpers

import io.iohk.atala.prism.walletsdk.domain.models.KeyPair

/**
 * Ed25519 is a variation of EdDSA
 */
actual object Ed25519 {
    actual fun createKeyPair(): KeyPair {
        throw NotImplementedError()
    }
}
