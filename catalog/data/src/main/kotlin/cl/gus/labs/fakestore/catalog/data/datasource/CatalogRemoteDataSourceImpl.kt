package cl.gus.labs.fakestore.catalog.data.datasource

import cl.gus.labs.fakestore.catalog.data.api.CatalogApi
import cl.gus.labs.fakestore.catalog.data.dto.ProductDto
import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.core.network.call.executeCall
import cl.gus.labs.fakestore.shared.kernel.AppError
import javax.inject.Inject

internal class CatalogRemoteDataSourceImpl @Inject constructor(
    private val api: CatalogApi,
) : CatalogRemoteDataSource {

    override suspend fun fetchCatalog(): Either<AppError, List<ProductDto>> =
        executeCall { api.getProducts() }
}
