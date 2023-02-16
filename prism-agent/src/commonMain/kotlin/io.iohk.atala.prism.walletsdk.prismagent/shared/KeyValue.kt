package io.iohk.atala.prism.walletsdk.prismagent.shared

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class KeyValue(
    val key: String,
    val value: String,
)
