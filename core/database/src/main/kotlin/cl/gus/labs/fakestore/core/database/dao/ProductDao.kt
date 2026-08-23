package cl.gus.labs.fakestore.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE (:category IS NULL OR category = :category) ORDER BY id ASC")
    fun observeAll(category: String?): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Int): Flow<ProductEntity?>

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE id NOT IN (:ids)")
    suspend fun deleteMissing(ids: List<Int>)

    @Transaction
    suspend fun syncAll(products: List<ProductEntity>) {
        upsertAll(products)
        deleteMissing(products.map(ProductEntity::id))
    }
}
