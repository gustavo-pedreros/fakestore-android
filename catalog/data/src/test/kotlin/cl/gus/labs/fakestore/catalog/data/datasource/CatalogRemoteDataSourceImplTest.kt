package cl.gus.labs.fakestore.catalog.data.datasource

import cl.gus.labs.fakestore.catalog.data.api.CatalogApi
import cl.gus.labs.fakestore.catalog.data.dto.ProductDto
import cl.gus.labs.fakestore.catalog.data.dto.RatingDto
import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.core.network.config.OkHttpClientFactory
import cl.gus.labs.fakestore.core.network.config.RetrofitFactory
import cl.gus.labs.fakestore.shared.kernel.AppError
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CatalogRemoteDataSourceImpl against a real Retrofit pipeline")
class CatalogRemoteDataSourceImplTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: CatalogRemoteDataSource

    @BeforeEach
    fun setUp() {
        server = MockWebServer().apply { start() }
        val client = OkHttpClientFactory.create(interceptors = emptyList())
        val retrofit = RetrofitFactory.create(server.url("/").toString(), client)
        val api = retrofit.create(CatalogApi::class.java)
        dataSource = CatalogRemoteDataSourceImpl(api)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    @DisplayName("decodes fakestoreapi.com's real payload shape into a list of ProductDto")
    fun decodesRealPayloadShape() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    [
                        {
                            "id": 1,
                            "title": "widget",
                            "price": 9.99,
                            "description": "a widget",
                            "category": "misc",
                            "image": "https://example.com/widget.png",
                            "rating": { "rate": 4.5, "count": 10 }
                        }
                    ]
                    """.trimIndent(),
                ),
        )

        val result = dataSource.fetchCatalog()

        assertEquals(
            listOf(
                ProductDto(
                    id = 1,
                    title = "widget",
                    price = 9.99,
                    description = "a widget",
                    category = "misc",
                    imageUrl = "https://example.com/widget.png",
                    rating = RatingDto(rate = 4.5, count = 10),
                ),
            ),
            (result as Either.Success).value,
        )
    }

    @Test
    @DisplayName("ignores unknown keys in the response")
    fun ignoresUnknownKeys() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    [
                        {
                            "id": 1,
                            "title": "widget",
                            "price": 9.99,
                            "description": "a widget",
                            "category": "misc",
                            "image": "https://example.com/widget.png",
                            "rating": { "rate": 4.5, "count": 10 },
                            "somethingNew": "ignore me"
                        }
                    ]
                    """.trimIndent(),
                ),
        )

        val result = dataSource.fetchCatalog()

        assertEquals(1, (result as Either.Success).value.size)
    }

    @Test
    @DisplayName("maps a 500 to AppError.Http")
    fun serverErrorMapsToHttp() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))

        val result = dataSource.fetchCatalog()

        val error = (result as Either.Error).error as AppError.Http
        assertEquals(500, error.httpStatus)
    }

    @Test
    @DisplayName("maps a malformed body to AppError.Unknown")
    fun malformedBodyMapsToUnknown() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("not json"),
        )

        val result = dataSource.fetchCatalog()

        assertTrue((result as Either.Error).error is AppError.Unknown)
    }
}
