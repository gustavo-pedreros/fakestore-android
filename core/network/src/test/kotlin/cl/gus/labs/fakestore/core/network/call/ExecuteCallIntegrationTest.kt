package cl.gus.labs.fakestore.core.network.call

import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.core.network.config.OkHttpClientFactory
import cl.gus.labs.fakestore.core.network.config.RetrofitFactory
import cl.gus.labs.fakestore.shared.kernel.AppError
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
private data class TestDto(val id: Int, val name: String)

private interface TestApi {
    @GET("thing")
    suspend fun getThing(): Response<TestDto>
}

@DisplayName("executeCall against a real Retrofit pipeline")
class ExecuteCallIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TestApi

    @BeforeEach
    fun setUp() {
        server = MockWebServer().apply { start() }
        val client = OkHttpClientFactory.create(interceptors = emptyList())
        val retrofit = RetrofitFactory.create(server.url("/").toString(), client)
        api = retrofit.create(TestApi::class.java)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    @DisplayName("decodes a well-formed body into Either.Success")
    fun happyPath() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":1,"name":"widget"}"""),
        )

        val result = executeCall { api.getThing() }

        assertEquals(TestDto(1, "widget"), (result as Either.Success).value)
    }

    @Test
    @DisplayName("a 200 with an empty body — like fakestoreapi.com's invalid-id response — maps to AppError.EmptyBody")
    fun emptyBodyOn200MapsToEmptyBody() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(""),
        )

        val result = executeCall { api.getThing() }

        assertEquals(AppError.EmptyBody, (result as Either.Error).error)
    }

    @Test
    @DisplayName("maps a real 404 to AppError.Http")
    fun realHttpError() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("not found"))

        val result = executeCall { api.getThing() }

        val error = (result as Either.Error).error as AppError.Http
        assertEquals(404, error.httpStatus)
    }
}