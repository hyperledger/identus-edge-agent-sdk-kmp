package org.hyperledger.identus.walletsdk.pluto.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.hyperledger.identus.walletsdk.SdkPlutoDb

actual class DbConnectionImpl actual constructor() : DbConnection {
    actual override var driver: SqlDriver? = null

    actual override suspend fun connectDb(context: Any?): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:prism.db")
        SdkPlutoDb.Schema.create(driver)
        this.driver = driver
        return driver
    }
}

actual val SqlDriver.isConnected: Boolean
    get() = (this as? JdbcSqliteDriver)?.getConnection()?.isValid(1) ?: false
