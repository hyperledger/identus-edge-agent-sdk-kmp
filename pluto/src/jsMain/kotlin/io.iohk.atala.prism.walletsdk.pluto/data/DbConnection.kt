package io.iohk.atala.prism.walletsdk.pluto.data

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.sqljs.initSqlDriver
import io.iohk.atala.prism.walletsdk.pluto.PrismPlutoDb
import kotlinx.coroutines.await

actual class DbConnection actual constructor() {
    actual var driver: SqlDriver? = null
    actual suspend fun connectDb(
        context: Any?,
    ): PrismPlutoDb {
        val driver = initSqlDriver(PrismPlutoDb.Schema).await()
        this.driver = driver
        return PrismPlutoDb(
            driver,
        )
    }
}

actual val SqlDriver.isConnected: Boolean
    get() = true
