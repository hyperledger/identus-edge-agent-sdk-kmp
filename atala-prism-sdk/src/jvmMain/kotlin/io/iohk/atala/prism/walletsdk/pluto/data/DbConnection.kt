package io.iohk.atala.prism.walletsdk.pluto.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.iohk.atala.prism.walletsdk.PrismPlutoDb

actual class DbConnection actual constructor() {
    actual var driver: SqlDriver? = null
    actual suspend fun connectDb(context: Any?): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PrismPlutoDb.Schema.create(driver)
        this.driver = driver
        return driver
    }
}

actual val SqlDriver.isConnected: Boolean
    get() = (this as? JdbcSqliteDriver)?.getConnection()?.isValid(1) ?: false
