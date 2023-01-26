package io.iohk.atala.prism.domain.models

data class PublicKey(
    val curve: KeyCurve,
    val value: String
)

data class CompressedPublicKey(
    val uncompressed: PublicKey,
    val value: String
)
