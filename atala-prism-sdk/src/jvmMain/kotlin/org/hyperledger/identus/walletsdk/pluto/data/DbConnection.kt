package org.hyperledger.identus.walletsdk.pluto.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.hyperledger.identus.walletsdk.SdkPlutoDb

actual class DbConnection actual constructor() {
    actual var driver: SqlDriver? = null
    actual suspend fun connectDb(context: Any?): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SdkPlutoDb.Schema.create(driver)
        this.driver = driver
        return driver
    }
}

actual val SqlDriver.isConnected: Boolean
    get() = (this as? JdbcSqliteDriver)?.getConnection()?.isValid(1) ?: false
