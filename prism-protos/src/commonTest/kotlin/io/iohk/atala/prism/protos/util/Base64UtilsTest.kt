package io.iohk.atala.prism.protos.util

import kotlin.test.Test
import kotlin.test.assertEquals

class Base64UtilsTest {
    @Test
    fun testEncoding() {
        val actual = Base64Utils.encode("subjects?_d=1~~~".encodeToByteArray())
        // Actual Base64 is c3ViamVjdHM/X2Q9MX5+fg==, but we need the URL-encoded version of it
        assertEquals("c3ViamVjdHM_X2Q9MX5-fg", actual)
    }

    @Test
    fun testDecoding() {
        val actual = Base64Utils.decode("c3ViamVjdHM_X2Q9MX5-fg")
        assertEquals("subjects?_d=1~~~", actual.decodeToString())
    }
}
