package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyProperties
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.VerifiableKey
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer

class Ed25519PublicKey(nativeValue: ByteArray) : PublicKey(), VerifiableKey {
    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<KeyProperties, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        keySpecification[CurveKey()] = Curve.ED25519.value
    }

    override fun verify(message: ByteArray, signature: ByteArray): Boolean {
        val key = Ed25519PrivateKeyParameters(raw.base64UrlDecoded.toByteArray().inputStream()).generatePublicKey()
        val verifier = Ed25519Signer()
        verifier.init(false, key)
        verifier.update(message, 0, message.size)
        return verifier.verifySignature(signature)
    }
}
