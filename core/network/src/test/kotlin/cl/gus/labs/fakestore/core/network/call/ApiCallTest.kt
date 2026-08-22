package cl.gus.labs.fakestore.core.network.call

import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.shared.kernel.AppError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("executeCall")
class ApiCallTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    @DisplayName("wraps a successful body in Either.Success")
    fun success() = runTest {
        val result = executeCall(dispatcher) { Response.success("payload") }

        assertEquals("payload", (result as Either.Success).value)
    }

    @Test
    @DisplayName("maps a successful response with a null body to AppError.EmptyBody")
    fun nullBody() = runTest {
        val result = executeCall(dispatcher) { Response.success<String>(null) }

        assertEquals(AppError.EmptyBody, (result as Either.Error).error)
    }

    @Test
    @DisplayName("maps an error response to AppError via toAppError")
    fun errorResponse() = runTest {
        val result = executeCall(dispatcher) {
            Response.error<String>(503, "".toResponseBody())
        }

        val error = assertInstanceOf(AppError.Http::class.java, (result as Either.Error).error)
        assertEquals(503, error.httpStatus)
    }

    @Test
    @DisplayName("maps a thrown IOException to AppError.Network")
    fun thrownException() = runTest {
        val result = executeCall<String>(dispatcher) { throw IOException("boom") }

        assertInstanceOf(AppError.Network::class.java, (result as Either.Error).error)
    }
}
