package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import junit.framework.TestCase.assertNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExportableImportableKeyTest {

    @Test
    fun testPEMKeyTypeFromString_whenCorrectStringProvided_thenSuccess() {
        val keyTypePrivate = PEMKeyType.fromString("-----BEGIN EC PRIVATE KEY-----")
        assertNotNull(keyTypePrivate)
        assertEquals(PEMKeyType.EC_PRIVATE_KEY, keyTypePrivate)
        val keyTypePublic = PEMKeyType.fromString("-----BEGIN EC PUBLIC KEY-----")
        assertNotNull(keyTypePublic)
        assertEquals(PEMKeyType.EC_PUBLIC_KEY, keyTypePublic)
    }

    @Test
    fun testPEMKeyTypeFromString_whenWrongStringProvided_thenReturnNull() {
        assertNull(PEMKeyType.fromString("-----BEGIN FAIL KEY-----"))
    }
}
