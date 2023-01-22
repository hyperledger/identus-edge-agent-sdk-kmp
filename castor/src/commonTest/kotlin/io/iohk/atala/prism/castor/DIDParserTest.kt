package io.iohk.atala.prism.castor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DIDParserTest {

    @Test
    fun it_should_test_valid_DIDs() {
        val didExample1 = "did:aaaaaa:aa:aaa"
        val didExample2 = "did:prism01:b2.-_%11:b4._-%11"
        val didExample3 =
            "did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"

        val parsedDID1 = DIDParser.parse(didExample1)
        val parsedDID2 = DIDParser.parse(didExample2)
        val parsedDID3 = DIDParser.parse(didExample3)

        assertEquals(parsedDID1.schema, "did")
        assertEquals(parsedDID1.method, "aaaaaa")
        assertEquals(parsedDID1.methodId, "aa:aaa")

        assertEquals(parsedDID2.schema, "did")
        assertEquals(parsedDID2.method, "prism01")
        assertEquals(parsedDID2.methodId, "b2.-_%11:b4._-%11")

        assertEquals(parsedDID3.schema, "did")
        assertEquals(parsedDID3.method, "prism")
        assertEquals(
            parsedDID3.methodId,
            "b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
        )
    }

    @Test
    fun it_should_test_invalid_DIDs() {
        val didExample1 = "idi:aaaaaa:aa:aaa"
        val didExample2 = "did:-prism-:aaaaa:aaaa"
        val didExample3 = "did:prism:aaaaaaaaaaa::"
        val didExample4 = "did::prism:aaaaaaaaaaa:aaaa"
        val didExample5 = "did:prism::aaaaaaaaaaa:bbbb"

        val exception = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser.parse(didExample1)
            }
        )
        assertEquals(exception.code, "InvalidDIDStringError")

        val exception2 = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser.parse(didExample2)
            }
        )
        assertEquals(exception2.code, "InvalidDIDStringError")

        val exception3 = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser.parse(didExample3)
            }
        )
        assertEquals(exception3.code, "InvalidDIDStringError")

        val exception4 = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser.parse(didExample4)
            }
        )
        assertEquals(exception4.code, "InvalidDIDStringError")

        val exception5 = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser.parse(didExample5)
            }
        )
        assertEquals(exception5.code, "InvalidDIDStringError")
    }
}
