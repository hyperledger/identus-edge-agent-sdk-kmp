package io.iohk.atala.prism.walletsdk.castor.resolvers

import io.iohk.atala.prism.walletsdk.castor.PEER
import io.iohk.atala.prism.walletsdk.castor.shared.CastorShared
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver

class PeerDIDResolver : DIDResolver {
    override val method: String = PEER
    override suspend fun resolve(didString: String): DIDDocument {
        return CastorShared.resolvePeerDID(didString)
    }
}
