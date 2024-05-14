package org.hyperledger.identus.walletsdk.castor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hyperledger.identus.walletsdk.castor.did.prismdid.PrismDIDMethodId
import org.hyperledger.identus.walletsdk.domain.models.CastorError
import kotlin.test.Test
import kotlin.test.assertFailsWith

class PrismDIDMethodIdTests {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun it_should_throwError_when_PrismDIDMethod_has_invalid_regex() = runTest {
        val validation1 = listOf("aggh123dgasj_-ddadsd", "adbadhj21231_-:0wqebnma")
        val validation2 = listOf("aggh12@3dgasj_-ddadsd", "adbadhj21231_-")
        val validation3 = listOf("aggh1/23dgasj_-ddadsd", "adbadhj21231_-")
        val validation4 = listOf("aggh1+23dgasj_-ddadsd", "adbadhj21231_-")

        assertFailsWith<CastorError.MethodIdIsDoesNotSatisfyRegex> {
            PrismDIDMethodId(validation1)
        }
        assertFailsWith<CastorError.MethodIdIsDoesNotSatisfyRegex> {
            PrismDIDMethodId(validation2)
        }
        assertFailsWith<CastorError.MethodIdIsDoesNotSatisfyRegex> {
            PrismDIDMethodId(validation3)
        }
        assertFailsWith<CastorError.MethodIdIsDoesNotSatisfyRegex> {
            PrismDIDMethodId(validation4)
        }
    }
}
