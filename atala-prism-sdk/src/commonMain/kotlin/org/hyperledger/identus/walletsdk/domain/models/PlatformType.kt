package org.hyperledger.identus.walletsdk.domain.models

/**
 * Represents the type of platform.
 *
 * This enum class defines the different supported platform types:
 * - JVM: Represents the Java Virtual Machine platform.
 * - ANDROID: Represents the Android platform.
 * - IOS: Represents the iOS platform.
 * - WEB: Represents the web platform.
 *
 * @property value The string representation of the platform type.
 */
enum class PlatformType(val value: String) {
    JVM("JVM"),
    ANDROID("ANDROID"),
    IOS("IOS"),
    WEB("WEB")
}
