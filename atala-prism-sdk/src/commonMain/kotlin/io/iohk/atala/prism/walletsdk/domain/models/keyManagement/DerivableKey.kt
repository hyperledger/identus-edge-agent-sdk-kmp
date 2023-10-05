package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import io.iohk.atala.prism.apollo.derivation.DerivationPath

interface DerivableKey {
    fun derive(derivationPath: DerivationPath): PrivateKey
}
