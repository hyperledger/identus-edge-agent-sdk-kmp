package org.hyperledger.identus.walletsdk.castor.did.prismdid

import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.DID
import kotlin.jvm.Throws

/**
 * Represents a LongFormPrismDID.
 *
 * @property did The DID associated with the LongFormPrismDID.
 * @property prismMethodId The PrismDIDMethodId instance associated with the LongFormPrismDID.
 * @property stateHash The state hash of the LongFormPrismDID.
 * @property encodedState The encoded state of the LongFormPrismDID.
 * @throws CastorError.InvalidLongFormDID if the methodId of the DID does not have 2 sections.
 */
data class LongFormPrismDID
@Throws(CastorError.InvalidLongFormDID::class)
constructor(val did: DID) {
    private val prismMethodId: PrismDIDMethodId
    val stateHash: String
    val encodedState: String

    init {
        val methodId = PrismDIDMethodId(
            did.methodId
        )

        if (methodId.sections.size != 2) {
            throw CastorError.InvalidLongFormDID()
        }

        val stateHash = methodId.sections.first()
        val encodedState = methodId.sections.last()

        this.prismMethodId = methodId
        this.stateHash = stateHash
        this.encodedState = encodedState
    }
}
