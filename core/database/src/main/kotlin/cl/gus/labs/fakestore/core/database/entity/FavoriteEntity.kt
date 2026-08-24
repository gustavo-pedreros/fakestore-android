package cl.gus.labs.fakestore.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val productId: Int,
    val updatedAt: Long,
    val syncState: SyncState,
)

enum class SyncState { LOCAL_ONLY }
