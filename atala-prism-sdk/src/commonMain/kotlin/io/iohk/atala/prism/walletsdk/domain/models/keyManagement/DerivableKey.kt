package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import io.iohk.atala.prism.apollo.derivation.DerivationPath

/**
 * This interface defines the functionality of a derivable key.
 */
interface DerivableKey {
    /**
     * Method to derive a key
     * @param derivationPath the derivation path used to dervie a key
     * @return a PrivateKey after being derived
     */
    fun derive(derivationPath: DerivationPath): PrivateKey
}
