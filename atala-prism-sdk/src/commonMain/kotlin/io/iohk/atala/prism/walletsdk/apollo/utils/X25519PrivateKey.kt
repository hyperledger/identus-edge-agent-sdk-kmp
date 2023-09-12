package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyProperties
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters

class X25519PrivateKey(nativeValue: ByteArray) : PrivateKey() {
    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<KeyProperties, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        keySpecification[CurveKey()] = Curve.X25519.value
    }

    override fun publicKey(): PublicKey {
        val xPrivateKey = X25519PrivateKeyParameters(raw, 0)
        val xPublicKey = xPrivateKey.generatePublicKey()
        return X25519PublicKey(xPublicKey.encoded)
    }
}
