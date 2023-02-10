package io.iohk.atala.prism.walletsdk.pluto.data

import io.iohk.atala.prism.walletsdk.pluto.PrismPlutoDb

expect class DbConnection {
    suspend fun connectDb(): PrismPlutoDb
}
