package io.iohk.atala.prism.walletsdk.castor.resolvers

import io.iohk.atala.prism.walletsdk.castor.shared.CastorShared
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
actual class PeerDIDResolver : DIDResolver {
    actual override val method: String = "peer"
    override fun resolve(didString: String): Promise<DIDDocument> {
        return GlobalScope.promise {
            CastorShared.resolvePeerDID(didString)
        }
    }
}
