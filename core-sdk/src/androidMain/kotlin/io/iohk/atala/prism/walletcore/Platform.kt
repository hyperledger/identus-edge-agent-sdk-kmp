package io.iohk.atala.prism.walletcore

internal actual object Platform {
    actual val OS: String
        get() = "Android ${android.os.Build.VERSION.SDK_INT}"
}