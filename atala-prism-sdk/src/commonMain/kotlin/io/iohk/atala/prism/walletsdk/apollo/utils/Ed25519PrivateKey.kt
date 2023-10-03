package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.utils.KMMEdPrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.SignableKey

class Ed25519PrivateKey(nativeValue: ByteArray) : PrivateKey(), SignableKey {

    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<String, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        keySpecification[CurveKey().property] = Curve.ED25519.value
    }

    override fun publicKey(): PublicKey {
        val public = KMMEdPrivateKey(raw).publicKey()
        return Ed25519PublicKey(public.raw)
    }

    override fun sign(message: ByteArray): ByteArray {
        val private = KMMEdPrivateKey(raw)
        return private.sign(message)
    }
}
