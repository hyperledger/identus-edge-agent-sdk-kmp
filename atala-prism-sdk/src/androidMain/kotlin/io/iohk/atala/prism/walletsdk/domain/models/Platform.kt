package io.iohk.atala.prism.walletsdk.domain.models

actual object Platform {
    actual val OS: String
        get() = "Android"
    actual val type: PlatformType
        get() = PlatformType.ANDROID
}
