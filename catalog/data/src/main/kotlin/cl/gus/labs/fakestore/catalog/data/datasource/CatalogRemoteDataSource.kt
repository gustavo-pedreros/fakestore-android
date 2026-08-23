package cl.gus.labs.fakestore.catalog.data.datasource

import cl.gus.labs.fakestore.catalog.data.dto.ProductDto
import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.shared.kernel.AppError

internal interface CatalogRemoteDataSource {
    suspend fun fetchCatalog(): Either<AppError, List<ProductDto>>
}
