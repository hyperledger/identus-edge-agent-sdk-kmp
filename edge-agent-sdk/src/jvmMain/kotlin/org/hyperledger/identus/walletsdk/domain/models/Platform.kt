package org.hyperledger.identus.walletsdk.domain.models

actual object Platform {
    actual val OS: String
        get() = "JVM - ${System.getProperty("java.runtime.name")} - ${System.getProperty("java.runtime.version")}"
    actual val type: PlatformType
        get() = PlatformType.JVM
}
