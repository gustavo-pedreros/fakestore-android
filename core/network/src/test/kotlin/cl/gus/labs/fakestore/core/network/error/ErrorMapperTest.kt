package cl.gus.labs.fakestore.core.network.error

import cl.gus.labs.fakestore.shared.kernel.AppError
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.BufferedSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@DisplayName("ErrorMapper")
class ErrorMapperTest {
    private val json = "application/json".toMediaType()

    @Test
    @DisplayName("maps an error response to AppError.Http with the status and raw body")
    fun mapsHttpError() {
        val error = Response.error<Any>(404, "not found".toResponseBody(json)).toAppError() as AppError.Http

        assertEquals(404, error.httpStatus)
        assertEquals("not found", error.rawBody)
    }

    @Test
    @DisplayName("maps a response with a blank body to AppError.Http")
    fun mapsBlankBody() {
        val error = Response.error<Any>(400, "".toResponseBody(json)).toAppError() as AppError.Http

        assertEquals(400, error.httpStatus)
        assertEquals("", error.rawBody)
    }

    @Test
    @DisplayName("maps an HttpException to AppError via its Response")
    fun httpExceptionDelegatesToResponse() {
        val response = Response.error<Any>(500, "boom".toResponseBody(json))

        val error = HttpException(response).toAppError() as AppError.Http

        assertEquals(500, error.httpStatus)
    }

    @Test
    @DisplayName("maps an IOException to AppError.Network")
    fun ioExceptionIsNetwork() {
        val error = IOException("offline").toAppError()

        assertTrue(error is AppError.Network)
    }

    @Test
    @DisplayName("maps an unexpected throwable to AppError.Unknown")
    fun otherThrowableIsUnknown() {
        val error = RuntimeException("weird").toAppError()

        assertTrue(error is AppError.Unknown)
    }

    @Test
    @DisplayName("maps a successful response, which carries no error body, to a null raw body")
    fun successfulResponseHasNoRawBody() {
        val error = Response.success("payload").toAppError() as AppError.Http

        assertEquals(200, error.httpStatus)
        assertNull(error.rawBody)
    }

    @Test
    @DisplayName("keeps the status when reading the error body fails")
    fun unreadableBodyStillYieldsStatus() {
        val unreadable = object : ResponseBody() {
            override fun contentType(): MediaType = json
            override fun contentLength(): Long = 1L
            override fun source(): BufferedSource = throw IOException("socket closed mid-read")
        }

        val error = Response.error<Any>(502, unreadable).toAppError() as AppError.Http

        assertEquals(502, error.httpStatus)
        assertNull(error.rawBody)
    }

    @Test
    @DisplayName("falls back to the status and message when an HttpException has lost its response")
    fun httpExceptionWithoutResponseFallsBack() {
        // HttpException holds its response in a transient field, so a round trip drops it.
        val revived = roundTrip(HttpException(Response.error<Any>(418, "teapot".toResponseBody(json))))

        val error = revived.toAppError() as AppError.Http

        assertNull(revived.response())
        assertEquals(418, error.httpStatus)
        assertEquals(revived.message, error.rawBody)
    }

    private fun roundTrip(exception: HttpException): HttpException {
        val bytes = ByteArrayOutputStream().also { out ->
            ObjectOutputStream(out).use { it.writeObject(exception) }
        }.toByteArray()

        return ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() } as HttpException
    }
}
