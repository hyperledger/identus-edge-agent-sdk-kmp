package io.iohk.atala.prism.walletsdk.pluto

import io.iohk.atala.prism.walletsdk.domain.models.PlutoError
import io.iohk.atala.prism.walletsdk.pluto.data.DbConnection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DatabaseConnectionTestCommon {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testDatabaseConnection_it_should_Establish_Connection_With_DB() = runTest {
        val pluto = PlutoImpl(DbConnection())
        assertEquals(pluto.isConnected, false)
        pluto.start()
        assertEquals(pluto.isConnected, true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testDatabaseConnection_it_should_throwException_when_query_runs_without_connection() = runTest {
        val pluto = PlutoImpl(DbConnection())
        assertEquals(pluto.isConnected, false)
        assertFailsWith<PlutoError.DatabaseConnectionError> {
            pluto.getAllCredentials()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testDatabaseConnection_it_should_throwException_when_query_runs_without_active_connection() = runTest {
        val pluto = PlutoImpl(DbConnection())
        assertEquals(pluto.isConnected, false)
        pluto.start()
        assertEquals(pluto.isConnected, true)
        pluto.stop()
        assertFailsWith<PlutoError.DatabaseConnectionError> {
            pluto.getAllCredentials()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testDatabaseConnection_it_should_throwException_when_same_start_called_twice() = runTest {
        val pluto = PlutoImpl(DbConnection())
        assertEquals(pluto.isConnected, false)
        pluto.start()
        assertFailsWith<PlutoError.DatabaseServiceAlreadyRunning> {
            pluto.start()
        }
    }
}
