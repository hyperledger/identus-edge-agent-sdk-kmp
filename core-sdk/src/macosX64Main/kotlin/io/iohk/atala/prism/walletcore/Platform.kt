package io.iohk.atala.prism.walletcore

import platform.Foundation.NSProcessInfo

internal actual object Platform {
    actual val OS: String
        get() {
            val processInfo = NSProcessInfo.processInfo()
            return "${processInfo.operatingSystemName()}-${processInfo.operatingSystemVersionString()}"
        }
}
