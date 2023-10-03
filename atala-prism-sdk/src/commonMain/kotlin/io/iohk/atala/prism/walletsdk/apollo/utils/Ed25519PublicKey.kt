package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.utils.KMMEdPublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.VerifiableKey

class Ed25519PublicKey(nativeValue: ByteArray) : PublicKey(), VerifiableKey {
    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<String, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        keySpecification[CurveKey().property] = Curve.ED25519.value
    }

    override fun verify(message: ByteArray, signature: ByteArray): Boolean {
        val public = KMMEdPublicKey(raw)
        return public.verify(message, signature)
    }
}
