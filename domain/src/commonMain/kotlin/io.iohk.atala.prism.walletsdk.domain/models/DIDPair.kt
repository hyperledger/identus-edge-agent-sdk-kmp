package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class DIDPair(
    val host: DID,
    val receiver: DID,
    val name: String?
)
