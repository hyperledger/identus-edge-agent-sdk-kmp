package org.hyperledger.identus.walletsdk.domain.models.keyManagement

import org.hyperledger.identus.apollo.derivation.DerivationPath

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
