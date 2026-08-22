package cl.gus.labs.fakestore.core.network.client

import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiServiceFactory @Inject constructor(
    private val retrofit: Retrofit,
) {
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)
}