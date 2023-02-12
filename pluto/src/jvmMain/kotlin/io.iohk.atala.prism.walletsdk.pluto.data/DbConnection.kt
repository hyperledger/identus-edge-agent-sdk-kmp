package io.iohk.atala.prism.walletsdk.pluto.data

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.iohk.atala.prism.walletsdk.pluto.PrismPlutoDb

actual class DbConnection actual constructor() {
    actual var driver: SqlDriver? = null
    actual suspend fun connectDb(context: Any?): PrismPlutoDb {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { driver ->
            PrismPlutoDb.Schema.create(driver)
        }
        this.driver = driver
        return PrismPlutoDb(
            driver,
        )
    }
}

actual val SqlDriver.isConnected: Boolean
    get() = (this as? JdbcSqliteDriver)?.getConnection()?.isValid(1) ?: false
