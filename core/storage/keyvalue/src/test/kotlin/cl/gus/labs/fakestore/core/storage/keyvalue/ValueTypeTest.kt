package cl.gus.labs.fakestore.core.storage.keyvalue

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

// The same number in each type: none may read another's.
private val samples: Map<ValueType<*>, Any> = mapOf(
    ValueType.StringType to "3",
    ValueType.BooleanType to true,
    ValueType.IntType to 3,
    ValueType.LongType to 3L,
    ValueType.DoubleType to 3.0,
)

@DisplayName("ValueType")
class ValueTypeTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    @DisplayName("keeps a value of its own type")
    fun keepsItsOwnType(type: ValueType<*>) {
        val value = samples.getValue(type)

        assertEquals(value, type.cast(value))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    @DisplayName("reads a value of another type as null")
    fun readsOtherTypesAsNull(type: ValueType<*>) {
        samples.filterKeys { it != type }.values.forEach { other ->
            assertNull(type.cast(other), "$other")
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    @DisplayName("reads an absent value as null")
    fun readsAbsentAsNull(type: ValueType<*>) {
        assertNull(type.cast(null))
    }

    companion object {
        @JvmStatic
        fun types(): List<ValueType<*>> = samples.keys.toList()
    }
}
