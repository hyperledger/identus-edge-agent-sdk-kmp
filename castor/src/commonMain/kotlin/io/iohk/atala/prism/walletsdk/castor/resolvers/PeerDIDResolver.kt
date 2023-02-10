package io.iohk.atala.prism.walletsdk.castor.resolvers

import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver

expect class PeerDIDResolver : DIDResolver {
    override val method: String
}
