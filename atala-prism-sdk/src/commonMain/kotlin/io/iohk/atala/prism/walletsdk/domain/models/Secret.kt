package io.iohk.atala.prism.walletsdk.domain.models

data class SecretMaterialJWK(val value: String)

enum class SecretType(val value: String) {
    JsonWebKey2020("JsonWebKey2020")
}

data class Secret(
    val id: String,
    val type: SecretType,
    val secretMaterial: SecretMaterialJWK
)
