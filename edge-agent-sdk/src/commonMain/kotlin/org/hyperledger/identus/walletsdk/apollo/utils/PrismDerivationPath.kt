package org.hyperledger.identus.walletsdk.apollo.utils

import org.hyperledger.identus.apollo.derivation.DerivationAxis
import org.hyperledger.identus.apollo.derivation.DerivationPath

/**
 * Represents the derivation path of a Prism key, used for deriving child keys from a master key.
 *
 * @property walletPurpose The purpose of the wallet. Default value is 29.
 * @property didMethod The purpose of the DID method. Default value is 29.
 * @property didIndex The index of the DID. Default value is 0.
 * @property keyPurpose The purpose of the key.
 * @property keyIndex The index of the key.
 */
class PrismDerivationPath(
    walletPurpose: Int = 29,
    didMethod: Int = 29,
    didIndex: Int = 0,
    keyPurpose: Int,
    keyIndex: Int
) {

    /**
     * The derivation path represents the path used to derive a cryptographic key.
     *
     * @property derivationPath The actual derivation path represented as a [DerivationPath] object.
     */
    private val derivationPath: DerivationPath = DerivationPath(
        listOf(
            DerivationAxis.hardened(walletPurpose),
            DerivationAxis.hardened(didMethod),
            DerivationAxis.hardened(didIndex),
            DerivationAxis.hardened(keyPurpose),
            DerivationAxis.hardened(keyIndex)
        )
    )

    /**
     * Constructs a new instance of the class.
     *
     * @param walletPurpose the purpose of the wallet
     * @param didMethod the method used for DID (Decentralized Identifier) generation
     * @param didIndex the index of the DID
     * @param keyPurpose the purpose of the key
     * @param keyIndex the index of the key
     */
    constructor(
        walletPurpose: Int = 29,
        didMethod: Int = 29,
        didIndex: Int = 0,
        keyPurpose: KeyUsage,
        keyIndex: Int
    ) : this(walletPurpose, didMethod, didIndex, keyPurpose.value, keyIndex)

    /**
     * Returns a string representation of the object.
     *
     * The returned string is in the format `m/wallet-purpose`*/
    override fun toString(): String {
        //  m/wallet-purpose`/did-method`/did-index`/key-purpose`/key-index`
        return derivationPath.toString()
    }
}
