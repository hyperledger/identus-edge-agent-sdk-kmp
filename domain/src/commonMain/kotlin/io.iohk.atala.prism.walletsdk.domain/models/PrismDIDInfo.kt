package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class PrismDIDInfo(
    val did: DID,
    val keyPathIndex: Int? = 0,
    val alias: String? = null
)
