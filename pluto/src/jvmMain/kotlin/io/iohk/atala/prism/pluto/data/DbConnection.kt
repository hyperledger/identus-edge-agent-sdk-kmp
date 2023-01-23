package io.iohk.atala.prism.pluto.data

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.iohk.atala.prism.pluto.PrismPlutoDb

actual class DbConnection {
    actual suspend fun connectDb(): PrismPlutoDb {
        return createPrismPlutoDb(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))
    }
}
