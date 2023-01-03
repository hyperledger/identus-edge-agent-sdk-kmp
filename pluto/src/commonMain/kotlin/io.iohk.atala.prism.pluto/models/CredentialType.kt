package io.iohk.atala.prism.pluto.models

enum class CredentialType(val type: String) {
    JWT("jwt"),
    W3C("w3c"),
    Unknown("Unknown")
}
