package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyProperties
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.SignableKey
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer

class Ed25519PrivateKey(nativeValue: ByteArray) : PrivateKey(), SignableKey {

    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<KeyProperties, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        keySpecification[CurveKey()] = Curve.ED25519.value
    }

    override fun publicKey(): PublicKey {
        val private = Ed25519PrivateKeyParameters(raw, 0)
        val public = private.generatePublicKey()
        return Ed25519PublicKey(public.encoded)
    }

    override fun sign(message: ByteArray): ByteArray {
        val privateParams = Ed25519PrivateKeyParameters(raw, 0)
        val signer = Ed25519Signer()
        signer.init(true, privateParams)
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }
}
