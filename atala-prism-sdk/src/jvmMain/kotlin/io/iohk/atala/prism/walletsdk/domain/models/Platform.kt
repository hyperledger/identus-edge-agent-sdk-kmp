package io.iohk.atala.prism.walletsdk.domain.models

actual object Platform {
    actual val OS: String
        get() = "JVM"
    actual val type: PlatformType
        get() = PlatformType.JVM
}
