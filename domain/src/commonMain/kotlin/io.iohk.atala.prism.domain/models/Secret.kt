package io.iohk.atala.prism.domain.models

enum class SecretMaterial(val value: String) {
    JWK("JWK")
}

enum class SecretType(val value: String) {
    JsonWebKey2020("JsonWebKey2020")
}

data class Secret(
    val id: String,
    val type: SecretType,
    val secretMaterial: SecretMaterial
)
