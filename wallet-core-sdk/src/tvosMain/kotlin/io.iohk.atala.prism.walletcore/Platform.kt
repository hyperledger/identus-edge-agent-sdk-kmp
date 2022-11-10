package io.iohk.atala.prism.walletcore

import platform.UIKit.UIDevice

internal actual object Platform {
    actual val OS: String
        get() = "${UIDevice.currentDevice.systemName()}-${UIDevice.currentDevice.systemVersion}"
}