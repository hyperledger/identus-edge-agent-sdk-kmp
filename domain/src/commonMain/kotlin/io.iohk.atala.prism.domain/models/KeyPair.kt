package io.iohk.atala.prism.domain.models

data class KeyPair(
    val curve: KeyCurve? = KeyCurve.SECP256K1,
    val privateKey: PrivateKey,
    val publicKey: PublicKey
)

enum class KeyCurve(val value: String) {
    X25519("X25519"),
    ED25519("Ed25519"),
    SECP256K1("secp256k1")
}
