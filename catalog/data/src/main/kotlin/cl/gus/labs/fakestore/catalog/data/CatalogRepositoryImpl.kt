package cl.gus.labs.fakestore.catalog.data

import cl.gus.labs.fakestore.catalog.data.datasource.CatalogLocalDataSource
import cl.gus.labs.fakestore.catalog.data.datasource.CatalogRemoteDataSource
import cl.gus.labs.fakestore.catalog.data.dto.ProductDto
import cl.gus.labs.fakestore.catalog.data.mapper.toDomain
import cl.gus.labs.fakestore.catalog.data.mapper.toEntity
import cl.gus.labs.fakestore.catalog.domain.CatalogRepository
import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.core.common.result.map
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import cl.gus.labs.fakestore.shared.kernel.AppError
import cl.gus.labs.fakestore.shared.kernel.ProductId
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class CatalogRepositoryImpl @Inject constructor(
    private val remote: CatalogRemoteDataSource,
    private val local: CatalogLocalDataSource,
    private val clock: Clock,
) : CatalogRepository {

    override fun observeAll(category: Category?): Flow<List<Product>> =
        local.observeAll(category?.value).map { rows -> rows.map(ProductEntity::toDomain) }

    override fun observeById(id: ProductId): Flow<Product?> =
        local.observeById(id.value).map { row -> row?.toDomain() }

    override fun observeLastSyncedAt(): Flow<Instant?> = local.observeLastSyncedAt()

    override suspend fun refresh(): Either<AppError, Unit> =
        remote.fetchCatalog().map { dtos ->
            local.syncAll(dtos.map(ProductDto::toEntity))
            local.writeLastSyncedAt(clock.now())
        }
}
