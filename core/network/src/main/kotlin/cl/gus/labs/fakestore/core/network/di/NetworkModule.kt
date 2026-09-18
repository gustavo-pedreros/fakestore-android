package cl.gus.labs.fakestore.core.network.di

import cl.gus.labs.fakestore.core.network.BuildConfig
import cl.gus.labs.fakestore.core.network.config.NetworkConfig
import cl.gus.labs.fakestore.core.network.config.NetworkJson
import cl.gus.labs.fakestore.core.network.config.OkHttpClientFactory
import cl.gus.labs.fakestore.core.network.config.RetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = NetworkConfig.BASE_URL

    @Provides
    @Singleton
    fun provideJson(): Json = NetworkJson

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val interceptors = buildList {
            if (BuildConfig.DEBUG) {
                add(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }
        }
        return OkHttpClientFactory.create(interceptors)
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @BaseUrl baseUrl: String,
        client: OkHttpClient,
        json: Json,
    ): Retrofit = RetrofitFactory.create(baseUrl, client, json)
}