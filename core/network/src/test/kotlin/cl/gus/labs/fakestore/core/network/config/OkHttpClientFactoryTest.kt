package cl.gus.labs.fakestore.core.network.config

import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("OkHttpClientFactory")
class OkHttpClientFactoryTest {

    @Test
    @DisplayName("applies the configured timeout to connect, read and write")
    fun appliesTimeouts() {
        val client = OkHttpClientFactory.create(interceptors = emptyList())

        // Literal, not NetworkConfig.TIMEOUT_SECONDS: that would move both sides of the assertion.
        assertEquals(15_000, client.connectTimeoutMillis)
        assertEquals(15_000, client.readTimeoutMillis)
        assertEquals(15_000, client.writeTimeoutMillis)
    }

    @Test
    @DisplayName("retries on connection failure")
    fun retriesOnConnectionFailure() {
        assertTrue(OkHttpClientFactory.create(interceptors = emptyList()).retryOnConnectionFailure)
    }

    @Test
    @DisplayName("registers every interceptor it is given, in order")
    fun registersInterceptorsInOrder() {
        val first = Interceptor { chain -> chain.proceed(chain.request()) }
        val second = Interceptor { chain -> chain.proceed(chain.request()) }

        val client = OkHttpClientFactory.create(listOf(first, second))

        assertEquals(listOf(first, second), client.interceptors)
    }

    @Test
    @DisplayName("adds a body-level logging interceptor only for a debug build")
    fun logsOnlyInDebug() {
        val debug = OkHttpClientFactory.loggingInterceptors(isDebug = true)
        val release = OkHttpClientFactory.loggingInterceptors(isDebug = false)

        assertEquals(1, debug.size)
        assertEquals(
            HttpLoggingInterceptor.Level.BODY,
            assertInstanceOf(HttpLoggingInterceptor::class.java, debug.single()).level,
        )
        assertEquals(emptyList<Interceptor>(), release)
    }

    @Test
    @DisplayName("builds the client on that same debug decision")
    fun clientCarriesTheDebugStack() {
        assertInstanceOf(
            HttpLoggingInterceptor::class.java,
            OkHttpClientFactory.create(isDebug = true).interceptors.single(),
        )
        assertEquals(emptyList<Interceptor>(), OkHttpClientFactory.create(isDebug = false).interceptors)
    }
}