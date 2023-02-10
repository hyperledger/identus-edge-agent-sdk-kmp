package io.iohk.atala.prism.walletsdk.pluto.data

import com.squareup.sqldelight.drivers.sqljs.initSqlDriver
import io.iohk.atala.prism.walletsdk.pluto.PrismPlutoDb
import io.iohk.atala.prism.walletsdk.pluto.shared.PlutoShared
import kotlinx.coroutines.await

actual class DbConnection {
    actual suspend fun connectDb(): PrismPlutoDb {
        val driver = initSqlDriver(PrismPlutoDb.Schema)
        return PlutoShared.createPrismPlutoDb(driver.await())
    }
}
