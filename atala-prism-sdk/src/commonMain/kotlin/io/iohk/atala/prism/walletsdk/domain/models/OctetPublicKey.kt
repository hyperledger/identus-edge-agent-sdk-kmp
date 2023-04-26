package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class OctetPublicKey
@JvmOverloads
constructor(@EncodeDefault val kty: String = "OKP", val crv: String, val x: String)
