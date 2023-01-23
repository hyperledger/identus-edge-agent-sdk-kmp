package io.iohk.atala.prism.pluto.data

import com.squareup.sqldelight.db.SqlDriver
import io.iohk.atala.prism.pluto.PrismPlutoDb

expect class DbConnection {
    suspend fun connectDb(): PrismPlutoDb
}

fun createPrismPlutoDb(driver: SqlDriver): PrismPlutoDb {
    return PrismPlutoDb(driver)
}
