package io.iohk.atala.prism.walletsdk.pluto.data

import com.squareup.sqldelight.db.SqlDriver
import io.iohk.atala.prism.walletsdk.PrismPlutoDb

expect class DbConnection() {
    var driver: SqlDriver?
    suspend fun connectDb(context: Any?): PrismPlutoDb
}

expect val SqlDriver.isConnected: Boolean
