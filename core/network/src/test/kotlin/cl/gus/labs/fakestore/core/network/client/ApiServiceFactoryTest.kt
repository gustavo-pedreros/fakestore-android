package cl.gus.labs.fakestore.core.network.client

import cl.gus.labs.fakestore.core.network.config.OkHttpClientFactory
import cl.gus.labs.fakestore.core.network.config.RetrofitFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import retrofit2.Response
import retrofit2.http.GET

@Serializable
private data class ProductDto(val id: Int, val title: String)

private interface ProductApi {
    @GET("products/1")
    suspend fun getProduct(): Response<ProductDto>
}

private interface CartApi {
    @GET("carts/1")
    suspend fun getCart(): Response<ProductDto>
}

@DisplayName("ApiServiceFactory")
class ApiServiceFactoryTest {

    private lateinit var server: MockWebServer
    private lateinit var factory: ApiServiceFactory

    @BeforeEach
    fun setUp() {
        server = MockWebServer().apply { start() }
        val client = OkHttpClientFactory.create(interceptors = emptyList())
        factory = ApiServiceFactory(RetrofitFactory.create(server.url("/").toString(), client))
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    @DisplayName("creates a service that issues a real request against the configured base url")
    fun createsWorkingService() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":1,"title":"widget"}"""),
        )

        val response = factory.create(ProductApi::class.java).getProduct()

        assertEquals(ProductDto(1, "widget"), response.body())
        assertEquals("/products/1", server.takeRequest().path)
    }

    @Test
    @DisplayName("creates independent services for different interfaces off the same Retrofit")
    fun createsDistinctServices() = runTest {
        repeat(2) {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"id":1,"title":"widget"}"""),
            )
        }

        factory.create(ProductApi::class.java).getProduct()
        factory.create(CartApi::class.java).getCart()

        assertEquals("/products/1", server.takeRequest().path)
        assertEquals("/carts/1", server.takeRequest().path)
    }
}
