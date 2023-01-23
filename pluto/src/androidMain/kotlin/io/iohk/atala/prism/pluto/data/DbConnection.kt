package io.iohk.atala.prism.pluto.data

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import io.iohk.atala.prism.pluto.PrismPlutoDb

actual class DbConnection(private val context: Context) {
    actual suspend fun connectDb(): PrismPlutoDb {
        val driver = AndroidSqliteDriver(PrismPlutoDb.Schema, context)
        return createPrismPlutoDb(driver)
    }
}
