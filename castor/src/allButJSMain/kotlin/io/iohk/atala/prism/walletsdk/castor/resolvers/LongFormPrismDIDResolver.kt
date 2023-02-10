package io.iohk.atala.prism.walletsdk.castor.resolvers

import io.iohk.atala.prism.walletsdk.castor.shared.resolveLongFormPrismDID
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver

actual class LongFormPrismDIDResolver(
    private val apollo: Apollo,
) : DIDResolver {
    actual override val method: String = "prism"

    override suspend fun resolve(didString: String): DIDDocument {
        return resolveLongFormPrismDID(
            apollo = apollo,
            didString = didString
        )
    }
}
