package org.hyperledger.identus.walletsdk.pluto.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.hyperledger.identus.walletsdk.SdkPlutoDb
import org.hyperledger.identus.walletsdk.domain.models.PlutoError

/**
 * DbConnection class represents a connection to the database.
 */
actual class DbConnectionImpl actual constructor() : DbConnection {
    actual override var driver: SqlDriver? = null
    actual override suspend fun connectDb(context: Any?): SqlDriver {
        val androidContext: Context = (context as? Context) ?: throw PlutoError.DatabaseContextError()
        val driver = AndroidSqliteDriver(SdkPlutoDb.Schema, androidContext, "prism.db")
        this.driver = driver
        return driver
    }
}

/**
 * Represents the connection status of an SQL driver.
 */
actual val SqlDriver.isConnected: Boolean
    get() {
        return try {
            this.execute(null, "SELECT 1", 0).value == 0L
            // return this.executeQuery(null, "SELECT 1", 0).next()
        } catch (ex: Exception) {
            false
        }
    }
