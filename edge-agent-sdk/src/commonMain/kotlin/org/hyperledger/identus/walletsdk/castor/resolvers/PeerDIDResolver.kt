package org.hyperledger.identus.walletsdk.castor.resolvers

import org.hyperledger.identus.walletsdk.castor.PEER
import org.hyperledger.identus.walletsdk.castor.shared.CastorShared
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDResolver

/**
 * The [PeerDIDResolver] class is an implementation of the [DIDResolver] interface for resolving DID document using the Peer DID method.
 *
 * @see DIDResolver
 */
class PeerDIDResolver : DIDResolver {
    override val method: String = PEER

    /**
     * Resolves a DID document using the Peer DID method.
     *
     * @param didString the string representation of the DID
     * @return the resolved DID document
     */
    override suspend fun resolve(didString: String): DIDDocument {
        return CastorShared.resolvePeerDID(didString)
    }
}
