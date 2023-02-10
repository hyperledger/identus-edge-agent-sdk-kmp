package io.iohk.atala.prism.walletsdk.castor.io.iohk.atala.prism.walletsdk.castor.did.prismdid

import io.iohk.atala.prism.walletsdk.castor.did.prismdid.PrismDIDMethodId
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.DID

data class
LongFormPrismDID(val did: DID) {
    private val prismMethodId: PrismDIDMethodId
    val stateHash: String
    val encodedState: String

    init {
        val methodId = PrismDIDMethodId(
            did.methodId
        )

        if (methodId.sections.size !== 2) {
            throw CastorError.InvalidLongFormDID()
        }

        val stateHash = methodId.sections.first()
        val encodedState = methodId.sections.last()

        this.prismMethodId = methodId
        this.stateHash = stateHash
        this.encodedState = encodedState
    }
}
