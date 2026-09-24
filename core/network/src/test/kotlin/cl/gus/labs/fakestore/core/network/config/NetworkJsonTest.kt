package cl.gus.labs.fakestore.core.network.config

import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@Serializable
private data class Product(val id: Int, val title: String)

@DisplayName("NetworkJson")
class NetworkJsonTest {

    @Test
    @DisplayName("ignores keys the DTO does not declare")
    fun ignoresUnknownKeys() {
        // fakestoreapi.com returns rating and category, which the DTO above does not model.
        val payload = """{"id":1,"title":"widget","category":"tools","rating":{"rate":4.5}}"""

        assertEquals(Product(1, "widget"), NetworkJson.decodeFromString<Product>(payload))
    }

    @Test
    @DisplayName("still fails when a declared key is missing")
    fun failsOnMissingKey() {
        assertThrows(SerializationException::class.java) {
            NetworkJson.decodeFromString<Product>("""{"id":1}""")
        }
    }
}