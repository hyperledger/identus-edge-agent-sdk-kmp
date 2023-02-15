package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class Mediator(
    val id: String,
    val mediatorDID: DID,
    val hostDID: DID,
    val routingDID: DID,
)
