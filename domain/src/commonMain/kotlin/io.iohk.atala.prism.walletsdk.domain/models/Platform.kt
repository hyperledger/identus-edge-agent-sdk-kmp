package io.iohk.atala.prism.walletsdk.domain.models

expect object Platform {
    val OS: String
    val type: PlatformType
}
