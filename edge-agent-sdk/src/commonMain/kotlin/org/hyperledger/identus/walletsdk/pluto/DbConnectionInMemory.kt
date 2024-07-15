package org.hyperledger.identus.walletsdk.pluto

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.hyperledger.identus.walletsdk.SdkPlutoDb
import org.hyperledger.identus.walletsdk.pluto.data.DbConnection

class DbConnectionInMemory : DbConnection {
    override var driver: SqlDriver? = null

    override suspend fun connectDb(context: Any?): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SdkPlutoDb.Schema.create(driver)
        this.driver = driver
        return driver
    }
}
