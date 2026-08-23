package cl.gus.labs.fakestore.catalog.data.datasource

import cl.gus.labs.fakestore.core.database.dao.ProductDao
import cl.gus.labs.fakestore.core.database.dao.SyncMetadataDao
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import cl.gus.labs.fakestore.core.database.entity.SyncMetadataEntity
import javax.inject.Inject
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class CatalogLocalDataSourceImpl @Inject constructor(
    private val productDao: ProductDao,
    private val syncMetadataDao: SyncMetadataDao,
) : CatalogLocalDataSource {

    override fun observeAll(category: String?): Flow<List<ProductEntity>> =
        productDao.observeAll(category).distinctUntilChanged()

    override fun observeById(id: Int): Flow<ProductEntity?> =
        productDao.observeById(id).distinctUntilChanged()

    override suspend fun syncAll(rows: List<ProductEntity>) = productDao.syncAll(rows)

    override fun observeLastSyncedAt(): Flow<Instant?> =
        syncMetadataDao.observeByScope(Scope)
            .map { it?.lastSyncedAt?.let(Instant::fromEpochMilliseconds) }
            .distinctUntilChanged()

    override suspend fun writeLastSyncedAt(at: Instant) =
        syncMetadataDao.upsert(SyncMetadataEntity(Scope, at.toEpochMilliseconds()))

    private companion object {
        const val Scope = "catalog"
    }
}
