package io.iohk.atala.prism.walletsdk.domain.models

actual object Platform {
    actual val type: PlatformType
        get() = PlatformType.ANDROID
    actual val OS: String
        get() = "Android ${android.os.Build.VERSION.SDK_INT}"
}
