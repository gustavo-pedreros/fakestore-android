package cl.gus.labs.fakestore.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Int): Flow<ProductEntity?>

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)
}
