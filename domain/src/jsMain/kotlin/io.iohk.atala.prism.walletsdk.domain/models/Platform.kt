package io.iohk.atala.prism.walletsdk.domain.models

actual object Platform {
    private val isNodeJs by lazy { js("(typeof process === 'object' && typeof require === 'function')").unsafeCast<Boolean>() }
    actual val type: PlatformType
        get() = PlatformType.WEB
    actual val OS: String
        get() = if (isNodeJs) {
            "NodeJS"
        } else {
            "JS"
        }
}
