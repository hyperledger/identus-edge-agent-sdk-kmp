package io.iohk.atala.prism.castor

import io.iohk.atala.prism.castor.antlrGrammar.InvalidDIDStringError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DIDParserTest {

    @Test
    fun it_should_test_valid_DIDs() {
        var didExample1 = "did:aaaaaa:aa:aaa"
        var didExample2 = "did:prism01:b2.-_%11:b4._-%11"
        var didExample3 =
            "did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"

        var parsedDID1 = DIDParser(didExample1).parse()
        var parsedDID2 = DIDParser(didExample2).parse()
        var parsedDID3 = DIDParser(didExample3).parse()

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
        var didExample1 = "idi:aaaaaa:aa:aaa"
        var didExample2 = "did:-prism-:aaaaa:aaaa"
        var didExample3 = "did:prism:aaaaaaaaaaa::"
        var didExample4 = "did::prism:aaaaaaaaaaa:aaaa"
        var didExample5 = "did:prism::aaaaaaaaaaa:bbbb"

        val exception = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser(didExample1).parse()
            }
        )
        assertEquals(exception.code, "InvalidDIDStringError")

        val exception2 = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser(didExample2).parse()
            }
        )
        assertEquals(exception2.code, "InvalidDIDStringError")

        val exception3 = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser(didExample3).parse()
            }
        )
        assertEquals(exception3.code, "InvalidDIDStringError")

        val exception4 = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser(didExample4).parse()
            }
        )
        assertEquals(exception4.code, "InvalidDIDStringError")

        val exception5 = assertFailsWith(
            exceptionClass = InvalidDIDStringError::class,
            block = {
                DIDParser(didExample5).parse()
            }
        )
        assertEquals(exception5.code, "InvalidDIDStringError")
    }
}
