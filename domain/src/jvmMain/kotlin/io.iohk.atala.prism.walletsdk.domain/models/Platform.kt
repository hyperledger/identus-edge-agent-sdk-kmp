package io.iohk.atala.prism.walletsdk.domain.models

actual object Platform {
    actual val type: PlatformType
        get() = PlatformType.JVM
    actual val OS: String
        get() = "JVM - ${System.getProperty("java.version")}"
}
