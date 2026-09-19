package cl.gus.labs.fakestore.core.network.config

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

private val json = "application/json".toMediaType()

private class FixedFactory(private val converter: Converter<ResponseBody, *>?) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? = converter
}

@DisplayName("EmptyBodyAwareConverterFactory")
class EmptyBodyAwareConverterFactoryTest {

    private val retrofit = Retrofit.Builder().baseUrl(NetworkConfig.BASE_URL).build()

    private fun converterOver(delegate: Converter<ResponseBody, *>?) =
        EmptyBodyAwareConverterFactory(FixedFactory(delegate))
            .responseBodyConverter(String::class.java, emptyArray(), retrofit)

    @Test
    @DisplayName("declines the type when the delegate has no converter for it")
    fun declinesUnsupportedType() {
        assertNull(converterOver(delegate = null))
    }

    @Test
    @DisplayName("returns null for a zero-length body without consulting the delegate")
    fun emptyBodyBecomesNull() {
        val converter = converterOver(Converter<ResponseBody, String> { error("must not be called") })

        assertNull(converter!!.convert("".toResponseBody(json)))
    }

    @Test
    @DisplayName("delegates a non-empty body to the wrapped converter")
    fun nonEmptyBodyIsDelegated() {
        val converter = converterOver(Converter<ResponseBody, String> { "decoded:" + it.string() })

        assertEquals("decoded:payload", converter!!.convert("payload".toResponseBody(json)))
    }
}
