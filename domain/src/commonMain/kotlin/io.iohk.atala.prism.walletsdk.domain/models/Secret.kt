package io.iohk.atala.prism.walletsdk.domain.models

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class SecretMaterialJWK(val value: String)

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class SecretType(val value: String) {
    JsonWebKey2020("JsonWebKey2020")
}

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Secret(
    val id: String,
    val type: SecretType,
    val secretMaterial: SecretMaterialJWK
)
