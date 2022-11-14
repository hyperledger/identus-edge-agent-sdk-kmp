package io.iohk.atala.prism.walletcore

internal actual object Platform {
    actual val OS: String
        get() = "JVM - ${System.getProperty("java.version")}"
}
