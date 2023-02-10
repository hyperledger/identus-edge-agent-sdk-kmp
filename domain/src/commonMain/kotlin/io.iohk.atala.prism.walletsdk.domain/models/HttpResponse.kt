package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class HttpResponse(val status: Int, val jsonString: JsonString)
