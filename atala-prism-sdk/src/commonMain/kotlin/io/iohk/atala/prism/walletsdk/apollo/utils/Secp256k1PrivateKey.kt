package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.SignableKey

class Secp256k1PrivateKey(nativeValue: ByteArray) : PrivateKey(), SignableKey {
    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<String, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        keySpecification[CurveKey().property] = Curve.SECP256K1.value
    }

    override fun publicKey(): PublicKey {
        return Secp256k1PublicKey(KMMECSecp256k1PrivateKey.secp256k1FromByteArray(raw).getPublicKey().raw)
    }

    override fun sign(message: ByteArray): ByteArray {
        val kmmPrivateKey = KMMECSecp256k1PrivateKey.secp256k1FromByteArray(raw)
        return kmmPrivateKey.sign(data = message)
    }
}
