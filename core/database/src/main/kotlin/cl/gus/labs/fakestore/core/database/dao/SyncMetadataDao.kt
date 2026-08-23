package cl.gus.labs.fakestore.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cl.gus.labs.fakestore.core.database.entity.SyncMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM sync_metadata WHERE scope = :scope")
    fun observeByScope(scope: String): Flow<SyncMetadataEntity?>

    @Upsert
    suspend fun upsert(metadata: SyncMetadataEntity)
}
