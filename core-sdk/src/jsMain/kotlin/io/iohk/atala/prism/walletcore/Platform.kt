package io.iohk.atala.prism.walletcore

internal actual object Platform {
    private val isNodeJs by lazy { js("(typeof process === 'object' && typeof require === 'function')").unsafeCast<Boolean>() }
    actual val OS: String
        get() = if (isNodeJs) {
            "NodeJS"
        } else {
            "JS"
        }
}
