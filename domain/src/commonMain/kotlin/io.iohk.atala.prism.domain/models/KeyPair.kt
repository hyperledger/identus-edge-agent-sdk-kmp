package io.iohk.atala.prism.domain.models

data class KeyPair(
    val curve: KeyCurve? = KeyCurve(Curves.SECP256K1.name),
    val privateKey: PrivateKey,
    val publicKey: PublicKey
)

data class KeyCurve(val name: String, val index: Int? = 0)

enum class Curves(val value: String) {
    X25519("X25519"),
    ED25519("Ed25519"),
    SECP256K1("secp256k1");
}

fun getKeyCurveByNameAndIndex(name: String, index: Int?): KeyCurve {
    return when (name) {
        Curves.X25519.value ->
            KeyCurve(Curves.X25519.value)

        Curves.ED25519.value ->
            KeyCurve(Curves.ED25519.value)

        Curves.SECP256K1.value ->
            KeyCurve(Curves.SECP256K1.value, index)

        else ->
            KeyCurve(Curves.SECP256K1.value, index)
    }
}
