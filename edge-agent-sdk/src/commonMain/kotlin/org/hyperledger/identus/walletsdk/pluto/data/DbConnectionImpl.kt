package org.hyperledger.identus.walletsdk.pluto.data

import app.cash.sqldelight.db.SqlDriver

interface DbConnection {
    var driver: SqlDriver?

    suspend fun connectDb(context: Any?): SqlDriver
}

/**
 * DbConnection class represents a connection to the database.
 */
expect class DbConnectionImpl() : DbConnection {
    override var driver: SqlDriver?

    /**
     * Establishes a connection to the database.
     *
     * @param context The context data required for establishing the connection. This can be null in some cases.
     *
     * @return The SdkPlutoDb instance representing the connection to the database.
     */
    override suspend fun connectDb(context: Any?): SqlDriver
}

/**
 * Represents the current connection status of the SQL driver.
 */
expect val SqlDriver.isConnected: Boolean
