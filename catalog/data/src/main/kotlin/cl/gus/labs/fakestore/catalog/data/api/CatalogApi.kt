package cl.gus.labs.fakestore.catalog.data.api

import cl.gus.labs.fakestore.catalog.data.dto.ProductDto
import retrofit2.Response
import retrofit2.http.GET

internal interface CatalogApi {
    @GET("products")
    suspend fun getProducts(): Response<List<ProductDto>>
}
