package io.iohk.atala.prism.walletsdk.pluto.data

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.iohk.atala.prism.walletsdk.pluto.PrismPlutoDb
import io.iohk.atala.prism.walletsdk.pluto.shared.PlutoShared

actual class DbConnection {
    actual suspend fun connectDb(): PrismPlutoDb {
        return PlutoShared.createPrismPlutoDb(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))
    }
}
