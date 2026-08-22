package cl.gus.labs.fakestore.core.network.config

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitFactory {

    private val jsonMediaType = "application/json".toMediaType()

    fun create(
        baseUrl: String,
        client: OkHttpClient,
        json: Json = NetworkJson,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(EmptyBodyAwareConverterFactory(json.asConverterFactory(jsonMediaType)))
        .build()
}
