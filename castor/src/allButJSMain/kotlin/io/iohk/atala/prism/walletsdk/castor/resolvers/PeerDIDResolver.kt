package io.iohk.atala.prism.walletsdk.castor.resolvers

import io.iohk.atala.prism.walletsdk.castor.shared.ResolvePeerDID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver

actual class PeerDIDResolver : DIDResolver {
    actual override val method: String = "peer"
    override suspend fun resolve(didString: String): DIDDocument {
        return ResolvePeerDID(didString)
    }
}
