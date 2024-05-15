package org.hyperledger.identus.walletsdk.logger

class EdgeLoggerMock : EdgeLogger {
    override fun debug(message: String, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("debug: [org.hyperledger.identus.walletsdk.Test] $message Metadata: $metaString")
    }

    override fun info(message: String, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("info: [org.hyperledger.identus.walletsdk.Test] $message Metadata: $metaString")
    }

    override fun warning(message: String, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("warning: [org.hyperledger.identus.walletsdk.Test] $message Metadata: $metaString")
    }

    override fun error(message: String, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("error: [org.hyperledger.identus.walletsdk.Test] $message Metadata: $metaString")
    }

    override fun error(error: Error, metadata: Array<Metadata>) {
        val metaString = metadata.joinToString { "${it.getValue(LogLevel.DEBUG)}\n" }
        println("error: [org.hyperledger.identus.walletsdk.Test] ${error.message} Metadata: $metaString")
    }
}
