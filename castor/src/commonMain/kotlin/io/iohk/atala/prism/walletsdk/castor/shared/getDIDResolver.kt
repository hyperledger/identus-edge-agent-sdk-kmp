package io.iohk.atala.prism.walletsdk.castor.shared

import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver
fun getDIDResolver(did: String, resolvers: Array<DIDResolver>): DIDResolver {
    val parsedDID = parseDID(did)
    return resolvers.find { it.method == parsedDID.method } ?: throw CastorError.NotPossibleToResolveDID()
}
