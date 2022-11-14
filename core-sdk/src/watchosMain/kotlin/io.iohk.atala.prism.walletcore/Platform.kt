package io.iohk.atala.prism.walletcore

import platform.WatchKit.WKInterfaceDevice

internal actual object Platform {
    actual val OS: String
        get() {
            val wkInterfaceDevice = WKInterfaceDevice.currentDevice()
            return "${wkInterfaceDevice.systemName}-${wkInterfaceDevice.systemVersion}"
        }
}
