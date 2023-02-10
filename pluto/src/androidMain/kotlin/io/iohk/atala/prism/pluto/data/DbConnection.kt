package io.iohk.atala.prism.walletsdk.pluto.data

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import io.iohk.atala.prism.walletsdk.pluto.PrismPlutoDb
import io.iohk.atala.prism.walletsdk.pluto.shared.PlutoShared

actual class DbConnection(private val context: Context) {
    actual suspend fun connectDb(): PrismPlutoDb {
        val driver = AndroidSqliteDriver(PrismPlutoDb.Schema, context)
        return PlutoShared.createPrismPlutoDb(driver)
    }
}
