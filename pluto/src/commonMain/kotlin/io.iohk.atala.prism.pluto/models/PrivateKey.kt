package io.iohk.atala.prism.pluto.models

data class PrivateKey(
    val curve: String, // TODO: Change to KeyCurve
    val value: String, // TODO: Change to Data
)
