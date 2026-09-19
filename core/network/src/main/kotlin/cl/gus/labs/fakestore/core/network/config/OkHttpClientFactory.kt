package cl.gus.labs.fakestore.core.network.config

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpClientFactory {
    fun create(interceptors: List<Interceptor>): OkHttpClient = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(true)
        connectTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        readTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        writeTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        interceptors.forEach(::addInterceptor)
    }.build()

    // A parameter rather than a read of BuildConfig, so a test can ask for either build's stack.
    fun create(isDebug: Boolean): OkHttpClient = create(loggingInterceptors(isDebug))

    internal fun loggingInterceptors(isDebug: Boolean): List<Interceptor> =
        if (isDebug) {
            listOf(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        } else {
            emptyList()
        }
}
