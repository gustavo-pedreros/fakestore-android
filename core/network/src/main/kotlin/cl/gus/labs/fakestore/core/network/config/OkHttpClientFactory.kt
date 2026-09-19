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

    /**
     * The client the app runs on. [isDebug] is the only thing that varies between builds, so it is a
     * parameter rather than a read of `BuildConfig`: the Hilt module passes the real flag, and a test
     * can ask for either build's interceptor stack without the DI graph.
     */
    fun create(isDebug: Boolean): OkHttpClient = create(loggingInterceptors(isDebug))

    internal fun loggingInterceptors(isDebug: Boolean): List<Interceptor> =
        if (isDebug) {
            listOf(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        } else {
            emptyList()
        }
}
