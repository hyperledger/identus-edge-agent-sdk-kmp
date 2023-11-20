package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.utils.KMMEdPublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.ExportableKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.JWK
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PEMKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.StorableKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.VerifiableKey

class Ed25519PublicKey(nativeValue: ByteArray) : PublicKey(), VerifiableKey, StorableKey, ExportableKey {
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

    override fun getPem(): String {
        return PEMKey(
            keyType = "EC PUBLIC KEY",
            keyData = raw
        ).pemEncoded()
    }

    override fun getJwk(): JWK {
        return JWK(
            kty = "OKP",
            crv = getProperty(CurveKey().property),
            x = raw.base64UrlEncoded
        )
    }

    override fun jwkWithKid(kid: String): JWK {
        return JWK(
            kty = "OKP",
            kid = kid,
            crv = getProperty(CurveKey().property),
            x = raw.base64UrlEncoded
        )
    }

    override val storableData: ByteArray
        get() = raw
    override val restorationIdentifier: String
        get() = "ed25519+pub"
}
