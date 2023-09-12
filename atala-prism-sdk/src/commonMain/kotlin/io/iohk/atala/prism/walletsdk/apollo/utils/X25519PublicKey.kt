package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyProperties
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey

class X25519PublicKey(nativeValue: ByteArray) : PublicKey() {
    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<KeyProperties, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        keySpecification[CurveKey()] = Curve.X25519.value
    }
}
