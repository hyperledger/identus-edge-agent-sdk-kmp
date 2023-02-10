package io.iohk.atala.prism.walletsdk.castor.shared

import io.iohk.atala.prism.walletsdk.castor.did.DIDParser
import io.iohk.atala.prism.walletsdk.domain.models.DID

fun parseDID(did: String): DID {
    return DIDParser.parse(did)
}
