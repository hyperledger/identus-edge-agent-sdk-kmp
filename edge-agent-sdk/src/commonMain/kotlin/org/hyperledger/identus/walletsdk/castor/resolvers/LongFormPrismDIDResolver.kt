package org.hyperledger.identus.walletsdk.castor.resolvers

import org.hyperledger.identus.walletsdk.castor.PRISM
import org.hyperledger.identus.walletsdk.castor.shared.CastorShared
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDResolver

/**
 * The LongFormPrismDIDResolver class is an implementation of the DIDResolver interface
 * for resolving DID document using the LongForm PRISM method.
 *
 * @param apollo The Apollo instance used for cryptographic operations.
 *
 * @see Apollo
 */
class LongFormPrismDIDResolver(
    private val apollo: Apollo
) : DIDResolver {
    override val method: String = PRISM

    /**
     * Resolves a DID document using the LongForm PRISM method.
     *
     * @param didString The string representation of the DID.
     * @return The resolved DID document.
     */
    override suspend fun resolve(didString: String): DIDDocument {
        return CastorShared.resolveLongFormPrismDID(
            apollo = apollo,
            didString = didString
        )
    }
}
