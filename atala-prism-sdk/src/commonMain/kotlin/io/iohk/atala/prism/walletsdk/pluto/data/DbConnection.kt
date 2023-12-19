package io.iohk.atala.prism.walletsdk.pluto.data

import com.squareup.sqldelight.db.SqlDriver
import io.iohk.atala.prism.walletsdk.PrismPlutoDb

/**
 * DbConnection class represents a connection to the database.
 */
expect class DbConnection() {
    var driver: SqlDriver?

    /**
     * Establishes a connection to the database.
     *
     * @param context The context data required for establishing the connection. This can be null in some cases.
     *
     * @return The PrismPlutoDb instance representing the connection to the database.
     */
    suspend fun connectDb(context: Any?): PrismPlutoDb
}

/**
 * Represents the current connection status of the SQL driver.
 */
expect val SqlDriver.isConnected: Boolean
