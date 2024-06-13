package org.hyperledger.identus.walletsdk.apollo.utils

import org.hyperledger.identus.apollo.derivation.DerivationAxis
import org.hyperledger.identus.apollo.derivation.DerivationPath

/**
 * m/wallet-purpose`/did-method`/did-index`/key-purpose`/key-index`
 * m/29'            /29'        /0'        /1'          /$index'
 */
class PrismDerivationPath(
    walletPurpose: Int = 29,
    didMethod: Int = 29,
    didIndex: Int = 0,
    keyPurpose: Int,
    keyIndex: Int
) {

    private val derivationPath: DerivationPath = DerivationPath(
        listOf(
            DerivationAxis.hardened(walletPurpose),
            DerivationAxis.hardened(didMethod),
            DerivationAxis.hardened(didIndex),
            DerivationAxis.hardened(keyPurpose),
            DerivationAxis.hardened(keyIndex)
        )
    )

    constructor(
        walletPurpose: Int = 29,
        didMethod: Int = 29,
        didIndex: Int = 0,
        keyPurpose: KeyUsage,
        keyIndex: Int
    ) : this(walletPurpose, didMethod, didIndex, keyPurpose.value, keyIndex)

    override fun toString(): String {
        //  m/wallet-purpose`/did-method`/did-index`/key-purpose`/key-index`
        return derivationPath.toString()
    }
}
