package cl.gus.labs.fakestore.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import cl.gus.labs.fakestore.core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT productId FROM favorites ORDER BY updatedAt DESC")
    fun observeIds(): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM favorites WHERE productId = :productId")
    suspend fun count(productId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun deleteById(productId: Int)

    @Transaction
    suspend fun toggle(favorite: FavoriteEntity) {
        if (count(favorite.productId) > 0) deleteById(favorite.productId) else insert(favorite)
    }
}
