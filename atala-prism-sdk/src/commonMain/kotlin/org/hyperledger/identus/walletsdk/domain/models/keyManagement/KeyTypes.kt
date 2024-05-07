package org.hyperledger.identus.walletsdk.domain.models.keyManagement

/**
 * Enumeration class representing different types of keys.
 *
 * @property type The string representation of the key type.
 */
enum class KeyTypes(val type: String) {
    EC("EC"),
    Curve25519("Curve25519")
}
