package io.iohk.atala.prism.walletsdk.castor.resolvers

import io.iohk.atala.prism.walletsdk.castor.shared.CastorShared
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
actual class LongFormPrismDIDResolver(
    private val apollo: Apollo,
) : DIDResolver {
    actual override val method: String = "prism"

    override fun resolve(didString: String): Promise<DIDDocument> {
        return GlobalScope.promise {
            CastorShared.resolveLongFormPrismDID(
                apollo = apollo,
                didString = didString
            )
        }
    }
}
