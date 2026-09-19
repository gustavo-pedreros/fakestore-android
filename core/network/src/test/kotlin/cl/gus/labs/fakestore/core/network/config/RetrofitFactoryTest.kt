package cl.gus.labs.fakestore.core.network.config

import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("RetrofitFactory")
class RetrofitFactoryTest {

    private val client = OkHttpClient()

    @Test
    @DisplayName("uses the base url it is given")
    fun usesGivenBaseUrl() {
        val retrofit = RetrofitFactory.create("https://example.test/api/", client)

        assertEquals("https://example.test/api/", retrofit.baseUrl().toString())
    }

    @Test
    @DisplayName("uses the call factory it is given")
    fun usesGivenClient() {
        assertEquals(client, RetrofitFactory.create(NetworkConfig.BASE_URL, client).callFactory())
    }

    @Test
    @DisplayName("wraps the serialization converter so empty bodies stay reachable")
    fun wrapsConverterForEmptyBodies() {
        val retrofit = RetrofitFactory.create(NetworkConfig.BASE_URL, client)

        assertTrue(retrofit.converterFactories().any { it is EmptyBodyAwareConverterFactory })
    }

    @Test
    @DisplayName("defaults to the shared lenient Json when none is passed")
    fun defaultsToNetworkJson() {
        val explicit = RetrofitFactory.create(NetworkConfig.BASE_URL, client, NetworkJson)
        val defaulted = RetrofitFactory.create(NetworkConfig.BASE_URL, client)

        assertEquals(
            explicit.converterFactories().map { it::class },
            defaulted.converterFactories().map { it::class },
        )
    }
}