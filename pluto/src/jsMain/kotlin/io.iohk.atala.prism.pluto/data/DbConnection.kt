package io.iohk.atala.prism.pluto.data

import com.squareup.sqldelight.drivers.sqljs.initSqlDriver
import io.iohk.atala.prism.pluto.PrismPlutoDb
import kotlinx.coroutines.await

actual class DbConnection {
    actual suspend fun connectDb(): PrismPlutoDb {
        val driver = initSqlDriver(PrismPlutoDb.Schema)
        return createPrismPlutoDb(driver.await())
    }
}
