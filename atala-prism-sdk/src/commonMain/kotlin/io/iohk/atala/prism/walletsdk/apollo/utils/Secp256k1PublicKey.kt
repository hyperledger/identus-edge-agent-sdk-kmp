package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PublicKey
import io.iohk.atala.prism.walletsdk.apollo.config.ECConfig
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CustomKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.VerifiableKey

class Secp256k1PublicKey(nativeValue: ByteArray) : PublicKey(), VerifiableKey {
    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<String, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        if (size == ECConfig.PUBLIC_KEY_COMPRESSED_BYTE_SIZE) {
            keySpecification[CustomKey("compressed").property] = "true"
        } else {
            keySpecification[CustomKey("compressed").property] = "false"
        }
        keySpecification[CurveKey().property] = Curve.SECP256K1.value
    }

    override fun verify(message: ByteArray, signature: ByteArray): Boolean {
        val kmmPublicKey = KMMECSecp256k1PublicKey.secp256k1FromBytes(raw)
        return kmmPublicKey.verify(
            signature = signature,
            data = message
        )
    }

    fun getEncodedCompressed(): ByteArray {
        return KMMECSecp256k1PublicKey(raw).getCompressed()
    }
}
