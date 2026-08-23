package cl.gus.labs.fakestore.catalog.domain

import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.shared.kernel.AppError
import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {

    fun observeAll(category: Category?): Flow<List<Product>>

    fun observeById(id: ProductId): Flow<Product?>

    suspend fun refresh(): Either<AppError, Unit>
}
