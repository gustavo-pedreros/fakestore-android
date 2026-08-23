package cl.gus.labs.fakestore.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val price: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val ratingRate: Double,
    val ratingCount: Int,
)
