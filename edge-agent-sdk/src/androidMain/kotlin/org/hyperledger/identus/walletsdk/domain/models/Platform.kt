package org.hyperledger.identus.walletsdk.domain.models

/**
 * The `Platform` object represents the platform on which the code is running.
 */
actual object Platform {
    /**
     * This variable represents the operating system on which the code is currently running.
     *
     * On Android, it returns a string with the Android version followed by the SDK level.
     * For example: "Android 10"
     *
     * @return The operating system of the device.
     */
    actual val OS: String
        get() = "Android ${android.os.Build.VERSION.SDK_INT}"

    /**
     * Represents the platform type.
     *
     * This actual property represents the current platform type. It is used to determine the type of the platform on which
     * the application is being executed. The possible platform types are JVM, ANDROID, IOS, and WEB.
     *
     * This property is read-only and can be accessed using the `type` property of the `PlatformType` class.
     *
     * Example usage:
     * ```
     * val platformType = PlatformType.ANDROID
     * ```
     *
     * @see PlatformType
     */
    actual val type: PlatformType = PlatformType.ANDROID
}
