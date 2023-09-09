package io.iohk.atala.prism.walletsdk.castor.resolvers

import io.iohk.atala.prism.walletsdk.castor.PRISM
import io.iohk.atala.prism.walletsdk.castor.shared.CastorShared
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver

class LongFormPrismDIDResolver(
    private val apollo: Apollo
) : DIDResolver {
    override val method: String = PRISM

    override suspend fun resolve(didString: String): DIDDocument {
        return CastorShared.resolveLongFormPrismDID(
            apollo = apollo,
            didString = didString
        )
    }
}
