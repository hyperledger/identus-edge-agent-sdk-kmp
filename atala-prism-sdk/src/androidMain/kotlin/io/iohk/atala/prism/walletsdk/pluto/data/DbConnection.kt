package io.iohk.atala.prism.walletsdk.pluto.data

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.iohk.atala.prism.walletsdk.PrismPlutoDb
import io.iohk.atala.prism.walletsdk.domain.models.PlutoError

actual class DbConnection actual constructor() {
    actual var driver: SqlDriver? = null
    actual suspend fun connectDb(context: Any?): PrismPlutoDb {
        val androidContext: Context = (context as? Context) ?: throw PlutoError.DatabaseContextError()
        val driver = AndroidSqliteDriver(PrismPlutoDb.Schema, androidContext, "prism.db")
        this.driver = driver
        return PrismPlutoDb(driver)
    }
}

actual val SqlDriver.isConnected: Boolean
    get() = true
