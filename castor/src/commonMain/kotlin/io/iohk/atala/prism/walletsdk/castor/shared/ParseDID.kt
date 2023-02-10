package io.iohk.atala.prism.walletsdk.castor.shared

import io.iohk.atala.prism.walletsdk.castor.did.DIDParser
import io.iohk.atala.prism.walletsdk.domain.models.DID

fun ParseDID(did: String): DID {
    return DIDParser.parse(did)
}
