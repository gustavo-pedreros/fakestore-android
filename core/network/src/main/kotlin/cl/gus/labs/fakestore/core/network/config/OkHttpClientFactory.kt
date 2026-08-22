package cl.gus.labs.fakestore.core.network.config

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object OkHttpClientFactory {
    fun create(interceptors: List<Interceptor>): OkHttpClient = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(true)
        connectTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        readTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        writeTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        interceptors.forEach(::addInterceptor)
    }.build()
}
