package org.hyperledger.identus.walletsdk.logger

import org.kotlincrypto.hash.sha2.SHA256
import org.lighthousegames.logging.logging
import java.util.UUID

/**
 * Constant value used to represent the privacy of metadata.
 *
 * This constant is used in the [Metadata] class to hide sensitive information
 * based on the provided [LogLevel]. When the [LogLevel] is lower than the required level
 * for accessing private metadata, this constant is returned instead of the actual value.
 *
 * @see Metadata
 */
private const val METADATA_PRIVACY_STR = "------"

/**
 * Property representing the unique identifier for the hashing log.
 * The value is generated as a random UUID string.
 *
 * @see UUID.randomUUID
 */
private val hashingLog = UUID.randomUUID().toString()

/**
 * Logger is an interface that defines methods for logging messages
 * with different log levels and metadata.
 */
interface Logger {
    /**
     * Logs a debug message with optional metadata.
     *
     * @param message The debug message.
     * @param metadata An array of metadata objects associated with the message (optional).
     */
    fun debug(message: String, metadata: Array<Metadata> = arrayOf())

    /**
     * Logs an information message with optional metadata.
     *
     * @param message The information message to be logged.
     * @param metadata Optional metadata to be associated with the information message.
     */
    fun info(message: String, metadata: Array<Metadata> = arrayOf())

    /**
     * Logs a warning message with optional metadata.
     *
     * @param message The warning message to log.
     * @param metadata An array of metadata objects associated with the warning message.
     */
    fun warning(message: String, metadata: Array<Metadata> = arrayOf())

    /**
     * Logs an error message with optional metadata.
     *
     * @param message The error message to be logged
     * @param metadata An array of Metadata objects containing additional information (optional)
     */
    fun error(message: String, metadata: Array<Metadata> = arrayOf())

    /**
     * This function is used to log an error with optional metadata.
     *
     * @param error The error to be logged.
     * @param metadata An array of metadata objects to be included in the log message. Defaults to an empty array if not provided.
     *
     * @see Logger.error
     * @see Metadata
     */
    fun error(error: Error, metadata: Array<Metadata> = arrayOf())
}

/**
 * Implementation of the Logger interface.
 *
 * @property category the LogComponent category for this logger
 */
class LoggerImpl(private val category: LogComponent) :
    Logger {

    private val log = logging(
        "[${category::class.qualifiedName}.$category]"

    )

    /**
     * Logs a debug message with optional metadata.
     *
     * @param message The debug message.
     * @param metadata An array of metadata objects associated with the message (optional).
     */
    override fun debug(message: String, metadata: Array<Metadata>) {
        if (category.logLevel != LogLevel.NONE) {
            log.debug { message }
            if (metadata.isNotEmpty()) {
                log.debug { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    /**
     * Logs an information message with optional metadata.
     *
     * @param message The information message to be logged.
     * @param metadata An array of metadata objects to be associated with the information message.
     */
    override fun info(message: String, metadata: Array<Metadata>) {
        if (category.logLevel != LogLevel.NONE) {
            log.info { message }
            if (metadata.isNotEmpty()) {
                log.info { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    /**
     * Logs a warning message with optional metadata.
     *
     * @param message The warning message to log.
     * @param metadata An array of metadata objects associated with the warning message.
     */
    override fun warning(message: String, metadata: Array<Metadata>) {
        if (category.logLevel != LogLevel.NONE) {
            log.warn { message }
            if (metadata.isNotEmpty()) {
                log.warn { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    /**
     * Logs an error message with optional metadata.
     *
     * @param message The error message to be logged.
     * @param metadata An array of metadata objects to be associated with the error message (optional).
     */
    override fun error(message: String, metadata: Array<Metadata>) {
        if (category.logLevel != LogLevel.NONE) {
            log.error { message }
            if (metadata.isNotEmpty()) {
                log.error { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    /**
     * Logs an error with optional metadata.
     *
     * @param error The error object to be logged.
     * @param metadata An array of metadata objects associated with the error (optional).
     */
    override fun error(error: Error, metadata: Array<Metadata>) {
        if (category.logLevel != LogLevel.NONE) {
            log.error { error.message }
            if (metadata.isNotEmpty()) {
                log.error { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    /**
     * Converts an array of Metadata objects to a String representation.
     *
     * @param array An array of Metadata objects to be converted.
     * @return The converted String representation of the Metadata objects, with each object's value separated by a new line.
     */
    private fun arrayToString(array: Array<Metadata>): String {
        return array.joinToString { "${it.getValue(category.logLevel)}\n" }
    }
}

/**
 * Sealed class representing different types of metadata.
 * This class has several subclasses: PublicMetadata, PrivateMetadata, PrivateMetadataByLevel, MaskedMetadata,
 * and MaskedMetadataByLevel.
 */
sealed class Metadata {
    /**
     * Represents public metadata with a key-value pair.
     *
     * @property key The key for the metadata.
     * @property value The value*/
    data class PublicMetadata(val key: String, val value: String) : Metadata() {
        /**
         *
         */
        override fun toString(): String {
            return value
        }
    }

    /**
     * Data class representing private metadata.
     * It extends the Metadata class.
     *
     * @property key The key of the metadata.
     * @property value The value of the metadata.
     *
     * @constructor Creates a new instance of the PrivateMetadata class.
     */
    data class PrivateMetadata(val key: String, val value: String) : Metadata() {
        /**
         * Returns a string representation of the object. In this case, it returns the value of the metadata.
         *
         * @return The value of the metadata as a string.
         */
        override fun toString(): String {
            return value
        }
    }

    /**
     * Represents private metadata with a specific level.
     *
     * @property category The category of the log component.
     * @property key The key of the metadata.
     * @property value The value of the metadata.
     * @property level The level of the metadata.
     */
    data class PrivateMetadataByLevel(
        val category: LogComponent,
        val key: String,
        val value: String,
        val level: LogLevel
    ) : Metadata() {
        /**
         * Returns a string representation of the object. In the case of the `toString` method, it returns the value of the metadata.
         *
         * @return The value of the metadata as a string.
         */
        override fun toString(): String {
            return value
        }
    }

    /**
     * Represents metadata with masked value.
     *
     * This class extends the abstract class Metadata and provides a masked representation
     * for the value of the metadata. The mask function used is SHA256 hashing.
     *
     * @property key The key of the metadata.
     * @property value The value of the metadata.
     * @constructor Creates an instance of MaskedMetadata.
     */
    data class MaskedMetadata(val key: String, val value: String) : Metadata() {
        /**
         * Returns a string representation of the object. In this case, it returns the value of the metadata.
         *
         * @return The value of the metadata as a string.
         */
        override fun toString(): String {
            return value
        }
    }

    /**
     * Represents masked metadata with a specific level of confidentiality.
     *
     * @property key The key of the metadata.
     * @property value The value of the metadata.
     * @property level The level of confidentiality associated with the metadata.
     */
    data class MaskedMetadataByLevel(
        val key: String,
        val value: String,
        val level: LogLevel
    ) : Metadata() {
        /**
         * Returns a string representation of the object. The string representation is the value property of the object.
         *
         * @return The value of the object as a string.
         */
        override fun toString(): String {
            return value
        }
    }

    /**
     * Returns the value associated with the given LogLevel.
     *
     * @param level The LogLevel to check against.
     * @return The value associated with the given LogLevel as a String.
     */
    fun getValue(level: LogLevel): String {
        return when (this) {
            is PublicMetadata -> value
            is PrivateMetadata -> METADATA_PRIVACY_STR
            is PrivateMetadataByLevel -> if (level.value < this.level.value) value else METADATA_PRIVACY_STR
            is MaskedMetadata -> sha256Masked(value)
            is MaskedMetadataByLevel -> if (level.value > this.level.value) value else sha256Masked(value)
        }
    }

    /**
     * Computes the SHA256 hash of the input string with a masking algorithm.
     *
     * @param input The input string to be hashed.
     * @return The SHA256 hash of the input string, represented as a hexadecimal string.
     */
    private fun sha256Masked(input: String): String {
        val sha256 = SHA256().digest((hashingLog + input).toByteArray())
        return sha256.joinToString("") { "%02x".format(it) }
    }
}

/**
 * The `LogLevel` enum represents different levels of logging.
 *
 * The available values are:
 * - INFO: Logs informational messages.
 * - DEBUG: Logs debug messages, typically used for troubleshooting.
 * - WARNING: Logs warning messages that indicate potential issues.
 * - ERROR: Logs error messages that indicate failures or errors in the system.
 * - NONE: Disables logging.
 *
 * @property value The numerical value associated with the log level.
 */
enum class LogLevel(val value: Int) {
    INFO(0),
    DEBUG(1),
    WARNING(2),
    ERROR(3),
    NONE(4)
}

/**
 * The `LogComponent` enum represents the different logging components in the system.
 *
 * The available values are:
 * - APOLLO
 * - CASTOR
 * - MERCURY
 * - PLUTO
 * - POLLUX
 * - EDGE_AGENT
 */
enum class LogComponent(var logLevel: LogLevel) {
    APOLLO(LogLevel.DEBUG),
    CASTOR(LogLevel.DEBUG),
    MERCURY(LogLevel.DEBUG),
    PLUTO(LogLevel.DEBUG),
    POLLUX(LogLevel.DEBUG),
    EDGE_AGENT(LogLevel.DEBUG)
}
