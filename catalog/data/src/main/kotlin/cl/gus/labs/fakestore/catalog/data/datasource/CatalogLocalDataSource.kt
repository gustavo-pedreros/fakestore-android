package cl.gus.labs.fakestore.catalog.data.datasource

import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

internal interface CatalogLocalDataSource {
    fun observeAll(category: String?): Flow<List<ProductEntity>>
    fun observeById(id: Int): Flow<ProductEntity?>
    suspend fun syncAll(rows: List<ProductEntity>)
    fun observeLastSyncedAt(): Flow<Instant?>
    suspend fun writeLastSyncedAt(at: Instant)
}
