package org.hyperledger.identus.walletsdk.logger

class LoggerMock : Logger {

    private var logLevel: LogLevel = LogLevel.NONE

    override fun debug(message: String, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("debug: [io.prism.kmm.sdk.Test] $message Metadata: $metaString")
    }

    override fun info(message: String, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("info: [io.prism.kmm.sdk.Test] $message Metadata: $metaString")
    }

    override fun warning(message: String, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("warning: [io.prism.kmm.sdk.Test] $message Metadata: $metaString")
    }

    override fun error(message: String, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("error: [io.prism.kmm.sdk.Test] $message Metadata: $metaString")
    }

    override fun error(error: Error, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("error: [io.prism.kmm.sdk.Test] ${error.message} Metadata: $metaString")
    }
}
