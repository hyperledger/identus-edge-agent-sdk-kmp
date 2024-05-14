package org.hyperledger.identus.walletsdk.domain.models

/**
 * Represents a secret material in the form of a JSON Web Key (JWK).
 *
 * @param value The string value of the JWK.
 */
data class SecretMaterialJWK(val value: String)

/**
 * Represents a type of secret.
 *
 * @property value The value associated with the secret type.
 */
enum class SecretType(val value: String) {
    JsonWebKey2020("JsonWebKey2020")
}

/**
 * Represents a secret, which is a piece of secret material and its type.
 */
data class Secret(
    val id: String,
    val type: SecretType,
    val secretMaterial: SecretMaterialJWK
)
