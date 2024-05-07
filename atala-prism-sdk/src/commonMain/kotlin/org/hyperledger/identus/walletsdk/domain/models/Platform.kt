package org.hyperledger.identus.walletsdk.domain.models

/**
 * The `Platform` class provides information about the current platform.
 *
 * This class is used for determining the type of platform and the operating system being used.
 * It provides the following properties:
 *
 * - `OS`: The name of the operating system as a string.
 * - `type`: The type of platform as an instance of the `PlatformType` enum class.
 *
 * Usage example:
 *
 * ```
 * val osName = Platform.OS
 * val platformType = Platform.type
 * ```
 */
expect object Platform {
    val OS: String
    val type: PlatformType
}
