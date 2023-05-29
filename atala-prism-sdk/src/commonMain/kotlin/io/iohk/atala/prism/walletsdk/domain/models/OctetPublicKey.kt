package io.iohk.atala.prism.walletsdk.domain.models

import io.iohk.atala.prism.walletsdk.mercury.OKP
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class OctetPublicKey
@JvmOverloads
constructor(@EncodeDefault val kty: String = OKP, val crv: String, val x: String)

@Serializable
data class OctetPrivateKey(val kty: String, val crv: String, val x: String, val d: String)
