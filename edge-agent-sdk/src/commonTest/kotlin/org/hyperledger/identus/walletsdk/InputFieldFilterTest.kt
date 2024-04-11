package org.hyperledger.identus.walletsdk

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.InputFieldFilter
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class InputFieldFilterTest {
    @Test
    fun test() {
        val model = InputFieldFilter(
            type = "number",
            pattern = null,
            enum = listOf(1.3F, 3f),
            const = null,
            value = "did:peer:asef8sdfj"
        )
        val jsonString = Json.encodeToString(model)

        val inputFieldFilter = Json.decodeFromString<InputFieldFilter>(jsonString)
        assertEquals(model.type, inputFieldFilter.type)
        assertEquals(model.pattern, inputFieldFilter.pattern)
        assertContentEquals(model.enum, inputFieldFilter.enum)
        assertContentEquals(model.const, inputFieldFilter.const)
        assertEquals(model.value, inputFieldFilter.value)
    }
}
