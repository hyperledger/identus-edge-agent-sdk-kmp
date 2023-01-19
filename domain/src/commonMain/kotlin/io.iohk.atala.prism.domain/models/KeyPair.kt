package io.iohk.atala.prism.domain.models
data class KeyPair(
    val curve: KeyCurve? = KeyCurve.secp256k1,
    val privateKey: PrivateKey,
    val publicKey: PublicKey
)

enum class KeyCurve(val value: String) {
    x25519("X25519"),
    ed25519("Ed25519"),
    secp256k1("secp256k1")

}
