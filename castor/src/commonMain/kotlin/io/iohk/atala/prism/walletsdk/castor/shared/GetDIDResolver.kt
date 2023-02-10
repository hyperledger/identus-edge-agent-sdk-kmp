package io.iohk.atala.prism.walletsdk.castor.shared

import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver
fun GetDIDResolver(did: String, resolvers: Array<DIDResolver>): DIDResolver {
    val parsedDID = ParseDID(did)
    return resolvers.find { it.method == parsedDID.method } ?: throw CastorError.NotPossibleToResolveDID()
}
