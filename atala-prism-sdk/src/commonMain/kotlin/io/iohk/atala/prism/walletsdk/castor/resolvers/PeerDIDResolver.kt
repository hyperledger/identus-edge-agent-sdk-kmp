package io.iohk.atala.prism.walletsdk.castor.resolvers

import io.iohk.atala.prism.walletsdk.castor.PEER
import io.iohk.atala.prism.walletsdk.castor.shared.CastorShared
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver

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
