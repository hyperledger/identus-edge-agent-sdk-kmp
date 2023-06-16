package io.iohk.atala.prism.walletsdk.logger

import io.iohk.atala.prism.apollo.uuid.UUID
import org.lighthousegames.logging.logging
import java.security.MessageDigest

private const val METADATA_PRIVACY_STR = "------"
private val hashingLog = UUID.randomUUID4().toString()

interface PrismLogger {
    fun debug(message: String, metadata: Array<Metadata> = arrayOf())
    fun info(message: String, metadata: Array<Metadata> = arrayOf())
    fun warning(message: String, metadata: Array<Metadata> = arrayOf())
    fun error(message: String, metadata: Array<Metadata> = arrayOf())
    fun error(error: Error, metadata: Array<Metadata> = arrayOf())
}

class PrismLoggerImpl(category: LogComponent) : PrismLogger {

    private val log = logging("[io.prism.kmm.sdk.$category]")

    private var logLevel: LogLevel = LogLevel.INFO

    override fun debug(message: String, metadata: Array<Metadata>) {
        if (logLevel != LogLevel.NONE) {
            log.debug { message }
            if (metadata.isNotEmpty()) {
                log.debug { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    override fun info(message: String, metadata: Array<Metadata>) {
        if (logLevel != LogLevel.NONE) {
            log.info { message }
            if (metadata.isNotEmpty()) {
                log.info { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    override fun warning(message: String, metadata: Array<Metadata>) {
        if (logLevel != LogLevel.NONE) {
            log.warn { message }
            if (metadata.isNotEmpty()) {
                log.warn { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    override fun error(message: String, metadata: Array<Metadata>) {
        if (logLevel != LogLevel.NONE) {
            log.error { message }
            if (metadata.isNotEmpty()) {
                log.error { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    override fun error(error: Error, metadata: Array<Metadata>) {
        if (logLevel != LogLevel.NONE) {
            log.error { error.message }
            if (metadata.isNotEmpty()) {
                log.error { "Metadata: ${arrayToString(metadata)}" }
            }
        }
    }

    private fun arrayToString(array: Array<Metadata>): String {
        return array.joinToString { "${it.getValue(logLevel)}\n" }
    }
}

sealed class Metadata {
    data class PublicMetadata(val key: String, val value: String) : Metadata() {
        override fun toString(): String {
            return value
        }
    }

    data class PrivateMetadata(val key: String, val value: String) : Metadata() {
        override fun toString(): String {
            return value
        }
    }

    data class PrivateMetadataByLevel(
        val category: LogComponent,
        val key: String,
        val value: String,
        val level: LogLevel
    ) : Metadata() {
        override fun toString(): String {
            return value
        }
    }

    data class MaskedMetadata(val key: String, val value: String) : Metadata() {
        override fun toString(): String {
            return value
        }
    }

    data class MaskedMetadataByLevel(
        val key: String,
        val value: String,
        val level: LogLevel
    ) : Metadata() {
        override fun toString(): String {
            return value
        }
    }

    fun getValue(level: LogLevel): String {
        return when (this) {
            is PublicMetadata -> value
            is PrivateMetadata -> METADATA_PRIVACY_STR
            is PrivateMetadataByLevel -> if (level.value < this.level.value) value else METADATA_PRIVACY_STR
            is MaskedMetadata -> sha256Masked(value)
            is MaskedMetadataByLevel -> if (level.value > this.level.value) value else sha256Masked(value)
        }
    }

    private fun sha256Masked(input: String): String {
        val sha256 = MessageDigest.getInstance("SHA-256").digest((hashingLog + input).toByteArray())
        return sha256.joinToString("") { "%02x".format(it) }
    }
}

enum class LogLevel(val value: Int) {
    INFO(0),
    DEBUG(1),
    WARNING(2),
    ERROR(3),
    NONE(4)
}

enum class LogComponent {
    APOLLO,
    CASTOR,
    MERCURY,
    PLUTO,
    POLLUX,
    PRISM_AGENT
}
